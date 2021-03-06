/*
 * Copyright (c) 2018, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 3 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

// GENERATED by com.oracle.truffle.r.ffi.codegen.FFIUpCallsIndexCodeGen class; DO NOT EDIT
// This file can be regenerated by running 'mx rfficodegen'
#ifndef RFFI_UPCALLSINDEX_H
#define RFFI_UPCALLSINDEX_H

#define ATTRIB_x 0
#define BODY_x 1
#define CAAR_x 2
#define CAD4R_x 3
#define CADDDR_x 4
#define CADDR_x 5
#define CADR_x 6
#define CAR_x 7
#define CDAR_x 8
#define CDDDR_x 9
#define CDDR_x 10
#define CDR_x 11
#define CLOENV_x 12
#define COMPLEX_x 13
#define DUPLICATE_ATTRIB_x 14
#define DispatchPRIMFUN_x 15
#define ENCLOS_x 16
#define FASTR_DATAPTR_x 17
#define FASTR_getConnectionChar_x 18
#define FORMALS_x 19
#define GetRNGstate_x 20
#define INTEGER_x 21
#define IS_S4_OBJECT_x 22
#define LENGTH_x 23
#define LEVELS_x 24
#define LOGICAL_x 25
#define NAMED_x 26
#define OBJECT_x 27
#define PRCODE_x 28
#define PRENV_x 29
#define PRINTNAME_x 30
#define PRSEEN_x 31
#define PRVALUE_x 32
#define PutRNGstate_x 33
#define RAW_x 34
#define RDEBUG_x 35
#define REAL_x 36
#define RSTEP_x 37
#define R_BaseEnv_x 38
#define R_BaseNamespace_x 39
#define R_BindingIsLocked_x 40
#define R_CHAR_x 41
#define R_CleanUp_x 42
#define R_ExternalPtrAddr_x 43
#define R_ExternalPtrProtected_x 44
#define R_ExternalPtrTag_x 45
#define R_FindNamespace_x 46
#define R_GetConnection_x 47
#define R_GlobalContext_x 48
#define R_GlobalEnv_x 49
#define R_Home_x 50
#define R_HomeDir_x 51
#define R_Interactive_x 52
#define R_LockBinding_x 53
#define R_MakeActiveBinding_x 54
#define R_MakeExternalPtr_x 55
#define R_MakeWeakRef_x 56
#define R_MakeWeakRefC_x 57
#define R_MethodsNamespace_x 58
#define R_NamespaceRegistry_x 59
#define R_NewHashedEnv_x 60
#define R_ParseVector_x 61
#define R_PreserveObject_x 62
#define R_PromiseExpr_x 63
#define R_ProtectWithIndex_x 64
#define R_ReadConnection_x 65
#define R_ReleaseObject_x 66
#define R_Reprotect_x 67
#define R_SetExternalPtrAddr_x 68
#define R_SetExternalPtrProtected_x 69
#define R_SetExternalPtrTag_x 70
#define R_TempDir_x 71
#define R_ToplevelExec_x 72
#define R_WeakRefKey_x 73
#define R_WeakRefValue_x 74
#define R_WriteConnection_x 75
#define R_alloc_x 76
#define R_compute_identical_x 77
#define R_do_MAKE_CLASS_x 78
#define R_do_new_object_x 79
#define R_do_slot_x 80
#define R_do_slot_assign_x 81
#define R_forceAndCall_x 82
#define R_getClassDef_x 83
#define R_getContextCall_x 84
#define R_getContextEnv_x 85
#define R_getContextFun_x 86
#define R_getContextSrcRef_x 87
#define R_getGlobalFunctionContext_x 88
#define R_getParentFunctionContext_x 89
#define R_has_slot_x 90
#define R_insideBrowser_x 91
#define R_isEqual_x 92
#define R_isGlobal_x 93
#define R_lsInternal3_x 94
#define R_nchar_x 95
#define R_new_custom_connection_x 96
#define R_tryEval_x 97
#define R_unLockBinding_x 98
#define Rf_GetOption1_x 99
#define Rf_NonNullStringMatch_x 100
#define Rf_PairToVectorList_x 101
#define Rf_PrintValue_x 102
#define Rf_ScalarComplex_x 103
#define Rf_ScalarInteger_x 104
#define Rf_ScalarLogical_x 105
#define Rf_ScalarRaw_x 106
#define Rf_ScalarReal_x 107
#define Rf_ScalarString_x 108
#define Rf_VectorToPairList_x 109
#define Rf_allocArray_x 110
#define Rf_allocList_x 111
#define Rf_allocMatrix_x 112
#define Rf_allocSExp_x 113
#define Rf_allocVector_x 114
#define Rf_any_duplicated_x 115
#define Rf_any_duplicated3_x 116
#define Rf_asChar_x 117
#define Rf_asCharacterFactor_x 118
#define Rf_asInteger_x 119
#define Rf_asLogical_x 120
#define Rf_asReal_x 121
#define Rf_asS4_x 122
#define Rf_bessel_i_x 123
#define Rf_bessel_i_ex_x 124
#define Rf_bessel_j_x 125
#define Rf_bessel_j_ex_x 126
#define Rf_bessel_k_x 127
#define Rf_bessel_k_ex_x 128
#define Rf_bessel_y_x 129
#define Rf_bessel_y_ex_x 130
#define Rf_beta_x 131
#define Rf_choose_x 132
#define Rf_classgets_x 133
#define Rf_coerceVector_x 134
#define Rf_cons_x 135
#define Rf_copyListMatrix_x 136
#define Rf_copyMatrix_x 137
#define Rf_copyMostAttrib_x 138
#define Rf_cospi_x 139
#define Rf_dbeta_x 140
#define Rf_dbinom_x 141
#define Rf_dcauchy_x 142
#define Rf_dchisq_x 143
#define Rf_defineVar_x 144
#define Rf_dexp_x 145
#define Rf_df_x 146
#define Rf_dgamma_x 147
#define Rf_dgeom_x 148
#define Rf_dhyper_x 149
#define Rf_digamma_x 150
#define Rf_dlnorm_x 151
#define Rf_dlogis_x 152
#define Rf_dnbeta_x 153
#define Rf_dnbinom_x 154
#define Rf_dnbinom_mu_x 155
#define Rf_dnchisq_x 156
#define Rf_dnf_x 157
#define Rf_dnorm4_x 158
#define Rf_dnt_x 159
#define Rf_dpois_x 160
#define Rf_dpsifn_x 161
#define Rf_dsignrank_x 162
#define Rf_dt_x 163
#define Rf_dunif_x 164
#define Rf_duplicate_x 165
#define Rf_duplicated_x 166
#define Rf_dweibull_x 167
#define Rf_dwilcox_x 168
#define Rf_error_x 169
#define Rf_errorcall_x 170
#define Rf_eval_x 171
#define Rf_findFun_x 172
#define Rf_findVar_x 173
#define Rf_findVarInFrame_x 174
#define Rf_findVarInFrame3_x 175
#define Rf_fprec_x 176
#define Rf_ftrunc_x 177
#define Rf_gammafn_x 178
#define Rf_getAttrib_x 179
#define Rf_gsetVar_x 180
#define Rf_inherits_x 181
#define Rf_install_x 182
#define Rf_installChar_x 183
#define Rf_isNull_x 184
#define Rf_isObject_x 185
#define Rf_isString_x 186
#define Rf_lbeta_x 187
#define Rf_lchoose_x 188
#define Rf_lengthgets_x 189
#define Rf_lgamma1p_x 190
#define Rf_lgammafn_x 191
#define Rf_lgammafn_sign_x 192
#define Rf_log1pexp_x 193
#define Rf_log1pmx_x 194
#define Rf_logspace_add_x 195
#define Rf_logspace_sub_x 196
#define Rf_match_x 197
#define Rf_mkCharLenCE_x 198
#define Rf_namesgets_x 199
#define Rf_ncols_x 200
#define Rf_nrows_x 201
#define Rf_pbeta_x 202
#define Rf_pbinom_x 203
#define Rf_pcauchy_x 204
#define Rf_pchisq_x 205
#define Rf_pentagamma_x 206
#define Rf_pexp_x 207
#define Rf_pf_x 208
#define Rf_pgamma_x 209
#define Rf_pgeom_x 210
#define Rf_phyper_x 211
#define Rf_plnorm_x 212
#define Rf_plogis_x 213
#define Rf_pnbeta_x 214
#define Rf_pnbinom_x 215
#define Rf_pnbinom_mu_x 216
#define Rf_pnchisq_x 217
#define Rf_pnf_x 218
#define Rf_pnorm5_x 219
#define Rf_pnorm_both_x 220
#define Rf_pnt_x 221
#define Rf_ppois_x 222
#define Rf_protect_x 223
#define Rf_psigamma_x 224
#define Rf_psignrank_x 225
#define Rf_pt_x 226
#define Rf_ptukey_x 227
#define Rf_punif_x 228
#define Rf_pweibull_x 229
#define Rf_pwilcox_x 230
#define Rf_qbeta_x 231
#define Rf_qbinom_x 232
#define Rf_qcauchy_x 233
#define Rf_qchisq_x 234
#define Rf_qexp_x 235
#define Rf_qf_x 236
#define Rf_qgamma_x 237
#define Rf_qgeom_x 238
#define Rf_qhyper_x 239
#define Rf_qlnorm_x 240
#define Rf_qlogis_x 241
#define Rf_qnbeta_x 242
#define Rf_qnbinom_x 243
#define Rf_qnbinom_mu_x 244
#define Rf_qnchisq_x 245
#define Rf_qnf_x 246
#define Rf_qnorm5_x 247
#define Rf_qnt_x 248
#define Rf_qpois_x 249
#define Rf_qsignrank_x 250
#define Rf_qt_x 251
#define Rf_qtukey_x 252
#define Rf_qunif_x 253
#define Rf_qweibull_x 254
#define Rf_qwilcox_x 255
#define Rf_rbeta_x 256
#define Rf_rbinom_x 257
#define Rf_rcauchy_x 258
#define Rf_rchisq_x 259
#define Rf_rexp_x 260
#define Rf_rf_x 261
#define Rf_rgamma_x 262
#define Rf_rgeom_x 263
#define Rf_rhyper_x 264
#define Rf_rlnorm_x 265
#define Rf_rlogis_x 266
#define Rf_rmultinom_x 267
#define Rf_rnbinom_x 268
#define Rf_rnbinom_mu_x 269
#define Rf_rnchisq_x 270
#define Rf_rnorm_x 271
#define Rf_rpois_x 272
#define Rf_rsignrank_x 273
#define Rf_rt_x 274
#define Rf_runif_x 275
#define Rf_rweibull_x 276
#define Rf_rwilcox_x 277
#define Rf_setAttrib_x 278
#define Rf_setVar_x 279
#define Rf_sign_x 280
#define Rf_sinpi_x 281
#define Rf_str2type_x 282
#define Rf_tanpi_x 283
#define Rf_tetragamma_x 284
#define Rf_trigamma_x 285
#define Rf_unprotect_x 286
#define Rf_unprotect_ptr_x 287
#define Rf_warning_x 288
#define Rf_warningcall_x 289
#define Rprintf_x 290
#define SETCAD4R_x 291
#define SETCADDDR_x 292
#define SETCADDR_x 293
#define SETCADR_x 294
#define SETCAR_x 295
#define SETCDR_x 296
#define SETLENGTH_x 297
#define SETLEVELS_x 298
#define SET_ATTRIB_x 299
#define SET_BODY_x 300
#define SET_CLOENV_x 301
#define SET_ENCLOS_x 302
#define SET_FORMALS_x 303
#define SET_NAMED_FASTR_x 304
#define SET_OBJECT_x 305
#define SET_RDEBUG_x 306
#define SET_RSTEP_x 307
#define SET_S4_OBJECT_x 308
#define SET_STRING_ELT_x 309
#define SET_SYMVALUE_x 310
#define SET_TAG_x 311
#define SET_TRUELENGTH_x 312
#define SET_TYPEOF_x 313
#define SET_VECTOR_ELT_x 314
#define STRING_ELT_x 315
#define SYMVALUE_x 316
#define TAG_x 317
#define TRUELENGTH_x 318
#define TYPEOF_x 319
#define UNSET_S4_OBJECT_x 320
#define VECTOR_ELT_x 321
#define exp_rand_x 322
#define forceSymbols_x 323
#define gdActivate_x 324
#define gdCircle_x 325
#define gdClip_x 326
#define gdClose_x 327
#define gdDeactivate_x 328
#define gdFlush_x 329
#define gdHold_x 330
#define gdLine_x 331
#define gdLocator_x 332
#define gdMetricInfo_x 333
#define gdMode_x 334
#define gdNewPage_x 335
#define gdOpen_x 336
#define gdPath_x 337
#define gdPolygon_x 338
#define gdPolyline_x 339
#define gdRaster_x 340
#define gdRect_x 341
#define gdSize_x 342
#define gdText_x 343
#define gdcSetColor_x 344
#define gdcSetFill_x 345
#define gdcSetFont_x 346
#define gdcSetLine_x 347
#define getCCallable_x 348
#define getConnectionClassString_x 349
#define getEmbeddingDLLInfo_x 350
#define getOpenModeString_x 351
#define getStrWidth_x 352
#define getSummaryDescription_x 353
#define isSeekable_x 354
#define norm_rand_x 355
#define octsize_x 356
#define registerCCallable_x 357
#define registerRoutines_x 358
#define restoreHandlerStacks_x 359
#define setDotSymbolValues_x 360
#define unif_rand_x 361
#define useDynamicSymbols_x 362

#define UPCALLS_TABLE_SIZE 363

#endif // RFFI_UPCALLSINDEX_H
