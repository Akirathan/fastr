#
# Copyright (c) 2016, 2018, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 3 only, as
# published by the Free Software Foundation.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 3 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 3 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
# or visit www.oracle.com if you need additional information or have any
# questions.
#
import os
import platform
import subprocess
import shutil
import mx
import mx_fastr

def _darwin_extract_realpath(lib, libpath):
    '''
    If libpath has a dependency on lib, return the path in the library, else None
    '''
    try:
        output = subprocess.check_output(['otool', '-L', libpath])
        lines = output.split('\n')
        for line in lines[1:]:
            if lib in line:
                parts = line.split(' ')
                return parts[0].strip()
        return None
    except subprocess.CalledProcessError:
        mx.abort('copylib: otool failed')

def _copylib(lib, libpath, plain_libpath_base, target):
    '''
    Just copying libxxx.so/dylib isn't sufficient as versioning is involved.
    The path is likely a symlink to libxxx.so.n, for example, but what is really
    important is the version that is encoded in the shared library itself.
    Unfortunately getting that info is OS specific.
    '''
    if platform.system() == 'Darwin':
        real_libpath = _darwin_extract_realpath('lib' + lib, libpath)
    else:
        try:
            if platform.system() == 'Linux':
                output = subprocess.check_output(['objdump', '-p', libpath])
            elif platform.system() == 'SunOS':
                output = subprocess.check_output(['elfdump', '-d', libpath])
            lines = output.split('\n')
            for line in lines:
                if 'SONAME' in line:
                    ix = line.find('lib' + lib + '.')
                    real_libpath = os.path.join(os.path.dirname(libpath), line[ix:])
        except subprocess.CalledProcessError:
            mx.abort('copylib: otool failed')
    # copy both files
    has_rpath = real_libpath.startswith('@rpath')
    if has_rpath:
        source_libpath = libpath
    else:
        source_libpath = real_libpath
    shutil.copyfile(source_libpath, os.path.join(target, os.path.basename(source_libpath)))
    mx.log('copied ' + lib + ' library from ' + source_libpath + ' to ' + target + ', real_libpath=' + real_libpath)
    os.chdir(target)
    mx.log('plain_libpath_base: ' + plain_libpath_base + ' libpath: ' + libpath + ' source_libpath: ' + source_libpath)
    if not has_rpath:
        if os.path.basename(real_libpath) != plain_libpath_base:
            # create a symlink
            if os.path.exists(plain_libpath_base):
                os.remove(plain_libpath_base)
            mx.log('ln -s ' + os.path.basename(real_libpath) + ' ' + plain_libpath_base)
            os.symlink(os.path.basename(real_libpath), plain_libpath_base)
        # On Darwin we change the id to use @rpath
        if platform.system() == 'Darwin':
            try:
                mx.log('install_name_tool -id @rpath/' + plain_libpath_base + ' ' + plain_libpath_base)
                subprocess.check_call(['install_name_tool', '-id', '@rpath/' + plain_libpath_base, plain_libpath_base])
                mx.log('install_name_tool --add_rpath @loader_path/ ' + plain_libpath_base)
                subprocess.check_call(['install_name_tool', '-add_rpath', '@loader_path/', plain_libpath_base])
            except subprocess.CalledProcessError:
                mx.abort('copylib: install_name_tool failed')

def copylib(args):
    '''
    This supports a configuration where no explicit setting (e.g. LD_LIBRARY_PATH) is
    required at runtime for the libraries that are required by FastR, e.g. pcre.
    The easy case is when the libraries are already installed with the correct versions
    in one of the directories, e.g./usr/lib, that is searched by default by the system linker -
    in which case no configuration is required.

    Otherwise, since systems vary considerably in where such libraries are located, the general solution
    is to copy libraries located in non-system locations into the FastR 'lib' directory. N.B. GNU R is
    even more picky than FastR about library versions and depends on a larger set, so the local
    copy embedded in FastR is built using PKG_LDFLAGS_OVERRIDE to specify the location of necessary external
    libraries. However, the result of this analysis isn't captured anywhere, so we re-analyze here.
    If PKG_LDFLAGS_OVERRIDE is unset, we assume the libraries are located in the system directories
    and do nothing.
    '''
    if os.environ.has_key('PKG_LDFLAGS_OVERRIDE'):
        parts = os.environ['PKG_LDFLAGS_OVERRIDE'].split(' ')
        ext = 'dylib' if platform.system() == 'Darwin' else 'so'
        lib_prefix = 'lib' + args[0] + '.'
        plain_libpath_base = lib_prefix + ext
        for part in parts:
            path = part.strip('"').lstrip('-L')
            if os.path.exists(path):
                for f in os.listdir(path):
                    if f.startswith(lib_prefix):
                        if os.path.exists(os.path.join(path, plain_libpath_base)):
                            f = plain_libpath_base
                        target_dir = args[1]
                        target = os.path.join(path, f)
                        if not os.path.exists(os.path.join(target_dir, f)):
                            if os.path.islink(target):
                                link_target = os.path.join(path, os.readlink(target))
                                mx.log('link target: ' + link_target)
                                if link_target == '/usr/lib/libSystem.B.dylib' or link_target == '/usr/lib/libSystem.dylib':
                                    # simply copy over the link to the system library
                                    os.symlink(link_target, os.path.join(target_dir, plain_libpath_base))
                                    return 0
                            _copylib(args[0], target, plain_libpath_base, args[1])
                        return 0

    if os.environ.has_key('FASTR_RELEASE'):
        # if args[0] == 'quadmath' and (mx.get_arch() == 'sparcv9' or mx.get_os() == 'solaris'):
        if mx.get_arch() == 'sparcv9' or mx.get_os() == 'solaris':
            return 0
        if os.environ.get('FASTR_RELEASE') == 'dev':
            mx.log(args[0] + ' not found in PKG_LDFLAGS_OVERRIDE, but required with FASTR_RELEASE')
            mx.log('the resulting FastR release build will not be portable to another system')
        else:
            mx.abort(args[0] + ' not found in PKG_LDFLAGS_OVERRIDE, but required with FASTR_RELEASE')

    mx.log(args[0] + ' not found in PKG_LDFLAGS_OVERRIDE, assuming system location')
    return 0

def updatelib(args):
    '''
    On Darwin, if we captured a library, then we patch up the references
    to it to use @rpath, for all the libs in the directory passed as argument .
    args:
      0 directory containing libs to patch (and may also contain the patchees)
    '''
    # These are not captured
    ignore_list = ['R', 'Rblas', 'Rlapack', 'jniboot']

    fastr_libdir = os.path.join(mx_fastr._fastr_suite.dir, 'lib')


    def locally_built(name):
        for ignore in ignore_list:
            x = 'lib' + ignore + '.dylib'
            if x == name:
                return True
        return False

    def get_captured_libs():
        cap_libs = []
        for lib in os.listdir(fastr_libdir):
            if not ('.dylib' in lib) or '.dylibl' in lib:
                # ignore non-libraries
                continue
            if locally_built(lib) or os.path.islink(os.path.join(fastr_libdir, lib)):
                continue
            cap_libs.append(lib)
        return cap_libs

    libdir = args[0]
    cap_libs = get_captured_libs()
    libs = []
    for lib in os.listdir(libdir):
        if (not ('.dylib' in lib or '.so' in lib)) or '.dylibl' in lib or '.sol' in lib:
            # ignore non-libraries
            continue
        if not os.path.islink(os.path.join(libdir, lib)):
            libs.append(lib)

    # for each of the libs, check whether they depend
    # on any of the captured libs, @rpath the dependency if so
    for lib in libs:
        targetlib = os.path.join(libdir, lib)
        for cap_lib in cap_libs:
            try:
                real_libpath = _darwin_extract_realpath(cap_lib, targetlib)
                if real_libpath and not '@rpath' in real_libpath:
                    cmd = ['install_name_tool', '-change', real_libpath, '@rpath/' + cap_lib, targetlib]
                    subprocess.check_call(cmd)
            except subprocess.CalledProcessError:
                mx.abort('update: install_name_tool failed')
