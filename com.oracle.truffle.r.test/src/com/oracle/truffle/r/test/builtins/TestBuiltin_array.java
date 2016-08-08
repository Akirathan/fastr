/*
 * This material is distributed under the GNU General Public License
 * Version 2. You may review the terms of this license at
 * http://www.gnu.org/licenses/gpl-2.0.html
 *
 * Copyright (c) 2014, Purdue University
 * Copyright (c) 2014, 2016, Oracle and/or its affiliates
 *
 * All rights reserved.
 */
package com.oracle.truffle.r.test.builtins;

import org.junit.Test;

import com.oracle.truffle.r.test.TestBase;

// Checkstyle: stop line length check
public class TestBuiltin_array extends TestBase {

    @Test
    public void testarray1() {
        assertEval("argv <- list(c(1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 2L), 59L, structure(list(dr = c('1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24', '25', '26', '27', '28', '29', '30', '31', '32', '33', '34', '35', '36', '37', '38', '39', '40', '41', '42', '43', '44', '45', '46', '47', '48', '49', '50', '51', '52', '53', '54', '55', '56', '57', '58', '59.5')), .Names = 'dr')); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray2() {
        assertEval("argv <- list(FALSE, FALSE, NULL); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray3() {
        assertEval("argv <- list(2.10239639473973e-05, c(1L, 1L), NULL); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray4() {
        assertEval("argv <- list(0, c(105L, 1L), list(NULL, structure('d', .Names = 'CURVE'))); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray5() {
        assertEval("argv <- list(structure(list(`1` = structure(list(lower = 13.2743449189798, est. = 24.8054653131966, upper = 46.3534067526313), .Names = c('lower', 'est.', 'upper'), row.names = 'reStruct.Rail.sd((Intercept))', class = 'data.frame')), .Names = '1'), c(1L, 1L), list('1', NULL)); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray6() {
        assertEval("argv <- list(0, 61, NULL); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray7() {
        assertEval("argv <- list(c(10L, 10L, 11L, 10L, 12L, 11L, 13L, 12L, 14L, 13L, 15L, 14L, 16L, 15L, 17L, 16L, 18L, 17L, 19L, 18L, 20L, 19L, 21L, 20L, 22L, 21L, 23L, 22L, 24L, 23L, 25L, 24L, 26L, 25L, 27L, 26L, 28L, 27L, 29L, 28L, 30L, 29L, 31L, 30L, 32L, 31L, 33L, 32L, 34L, 33L, 35L, 34L, 36L, 35L, 37L, 36L, 38L, 36L, 39L, 38L, 40L, 39L), c(2L, 31L), list(c('target', 'actual'), NULL)); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray8() {
        assertEval("argv <- list(c(NA, NA, NA, NA, NA, NA, 29L, NA, 71L, 39L, NA, NA, 23L, NA, NA, 21L, 37L, 20L, 12L, 13L, NA, NA, NA, NA, NA, NA, NA, NA, NA, NA), c(30L, 1L), NULL); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray9() {
        assertEval("argv <- list(integer(0), c(1L, 0L), structure(list('1', NULL), .Names = c('', ''))); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray10() {
        assertEval(Ignored.Unknown,
                        "argv <- list(c(FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE), c(5L, 16L), list(c('#ifdef', '\\\\Sexpr', 'build', 'install', 'render'), NULL)); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray11() {
        assertEval(Ignored.Unknown,
                        "argv <- list(list(NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL), 8L, list(c('1', '2', '3', '4', '5', '6', '7', '8'))); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray12() {
        assertEval("argv <- list(c(-Inf, -Inf, 0, 2, 4, 5, Inf, Inf, Inf, -Inf, -Inf, 0, 2, 4, 5, Inf, Inf, Inf, -Inf, -Inf, 0, 1, 3, 5, Inf, Inf, Inf, -Inf, -Inf, -Inf, 1.5, 3.2, 4.9, Inf, Inf, Inf, -Inf, -Inf, 0.300000000000001, 2, 3.7, Inf, Inf, Inf, Inf, -Inf, -Inf, 0.2, 2, 3.8, Inf, Inf, Inf, Inf, -Inf, -Inf, 0.4, 2, 3.6, Inf, Inf, Inf, Inf, -Inf, -Inf, 0.266666666666667, 2, 3.73333333333333, Inf, Inf, Inf, Inf, -Inf, -Inf, 0.275, 2, 3.725, Inf, Inf, Inf, Inf), c(9L, 9L), list(c('20%', '30%', '40%', '50%', '60%', '70%', '80%', '90%', '100%'), NULL)); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray13() {
        assertEval("argv <- list(NA, 1L, list('1')); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray14() {
        assertEval("argv <- list(logical(0), 0L, NULL); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray15() {
        assertEval("argv <- list(structure(c(-5.3088868291531, 5.2393213877113, -5.301817110509, 5.29234872074472), .Names = c('5%', '95%', '5%', '95%')), c(2, 2), list(c('5%', '95%'), NULL)); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray16() {
        assertEval("argv <- list(c(1L, 0L), 2L, structure(list(object = c('FALSE', NA)), .Names = 'object')); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray17() {
        assertEval("argv <- list(c('', '', ''), c(3, 1), NULL); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray18() {
        assertEval("argv <- list(structure(numeric(0), .Dim = c(0L, 0L)), 0L, NULL); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray19() {
        assertEval("argv <- list(structure(c(31.9166666666667, -5.77777777777778, -10.4101831674686, -2.63888888888889, NA), .Names = c('(Intercept)', 'woolB', 'tens.L', 'tensionM', 'tensionH')), c(5L, 1L), list(c('(Intercept)', 'woolB', 'tens.L', 'tensionM', 'tensionH'), NULL)); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray20() {
        assertEval(Ignored.Unknown,
                        "argv <- list(c(0.92317305817397+0i, 0.160449395256071+0.220125597679977i, 0.40353715410585+2.39063261466203i, -3.64092275386503+3.51619480964107i, -0.30877433127864+1.37503901638266i, -0.5590368753986+2.95994484328048i, 2.07117052177259-1.58552086053907i, 5.12796916272868+5.50114308371867i, 0.71791019962021-4.36295436036464i, 3.6182846955548+0.01693946731429i, 5.86560669896785+3.41674024963709i, 7.14153164455803+0i, 5.86560669896785-3.41674024963709i, 3.6182846955548-0.01693946731429i, 0.71791019962021+4.36295436036464i, 5.12796916272868-5.50114308371867i, 2.07117052177259+1.58552086053907i, -0.5590368753986-2.95994484328048i, -0.30877433127864-1.37503901638266i, -3.64092275386503-3.51619480964107i, 0.40353715410585-2.39063261466203i, 0.160449395256071-0.220125597679976i, 0.994686860835215+0i, -0.711636086238366+0.034977366507257i, -3.47255638259391-3.00654729467177i, -1.61617641806619-2.52564108817258i, -1.83729841635945+1.24025696654912i, -0.05940773912914+1.99807537840182i, 2.14861624215501+1.14547234755584i, -0.18935885218927+5.11711397439959i, 3.55025883223277-3.01463113510177i, 0.37587194655463-4.62160286369829i, -0.57999032040714+3.57394816552023i, -3.22078701201057+0i, -0.57999032040714-3.57394816552023i, 0.37587194655463+4.62160286369829i, 3.55025883223277+3.01463113510177i, -0.18935885218927-5.11711397439959i, 2.14861624215501-1.14547234755584i, -0.05940773912914-1.99807537840182i, -1.83729841635945-1.24025696654912i, -1.61617641806619+2.52564108817258i, -3.47255638259391+3.00654729467177i, -0.711636086238366-0.034977366507256i, -0.376031201145236+0i, 0.36561036190112-2.94822783523588i, 2.53378536984825+1.14599403212998i, -0.59345500414631-1.46249091231517i, -5.47371957596241-2.40983118775265i, 0.994698295196402+0.827012883372647i, 4.88614691865207-0.66440097322583i, -1.22869446246947-1.85036568311679i, 4.54719422944744-1.7507307644741i, -1.25805718969215-0.46461775748286i, -6.6950163960079-1.32606545879492i, -1.8510470181104-0i, -6.6950163960079+1.32606545879492i, -1.25805718969215+0.46461775748286i, 4.54719422944744+1.7507307644741i, -1.22869446246947+1.85036568311679i, 4.88614691865207+0.66440097322583i, 0.994698295196402-0.827012883372647i, -5.47371957596241+2.40983118775265i, -0.59345500414631+1.46249091231517i, 2.53378536984825-1.14599403212998i, 0.36561036190112+2.94822783523588i, 1.86949363581639+0i, 3.2510927680528+3.7297126359622i, 5.77117909703734-0.58113122596059i, -2.73489323319193-2.03739778844743i, 1.59256247378073-3.23882870600546i, -2.21652163259476+3.70287191787544i, -6.80966667821261-4.74346958471693i, -0.48551953206469-3.42445496113818i, -4.95350216815663-1.60107509096991i, -0.651322462114205+0.588393022429161i, 3.32067078328635+3.75999833207777i, -1.35013798358527+0i, 3.32067078328635-3.75999833207777i, -0.651322462114205-0.588393022429161i, -4.95350216815663+1.60107509096991i, -0.48551953206469+3.42445496113818i, -6.80966667821261+4.74346958471693i, -2.21652163259476-3.70287191787544i, 1.59256247378073+3.23882870600546i, -2.73489323319193+2.03739778844743i, 5.77117909703734+0.58113122596059i, 3.2510927680528-3.7297126359622i, -3.90806827793786+0i, -4.10078155861753-4.25996878161911i, -0.63461032994351-2.08074582601136i, -0.10593736514835-3.82022652091785i, 6.14817602783479+2.33657685886581i, 0.64431546852762-1.776774088028i, 3.43771282488202-3.00904523977379i, -3.6812061457129+3.53944567666635i, 3.07722382691467+4.5373840425762i, 3.3679046040028+7.20820407858926i, 7.47003475089893-0.4463480891006i, 13.9322715624418-0i, 7.47003475089893+0.4463480891006i, 3.3679046040028-7.20820407858926i, 3.07722382691467-4.5373840425762i, -3.6812061457129-3.53944567666635i, 3.43771282488202+3.00904523977379i, 0.64431546852762+1.776774088028i, 6.14817602783479-2.33657685886581i, -0.10593736514835+3.82022652091785i, -0.63461032994351+2.08074582601136i, -4.10078155861753+4.25996878161911i), c(22, 5), NULL); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray21() {
        assertEval("argv <- list(NA, c(1, 4), NULL); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray22() {
        assertEval("argv <- list(structure(c(-0.350406242534262, -0.350406242534262, -3.35040624253426, 0.649593757465738, 1.64959375746574, 17.755677101477, 7.755677101477, -11.3171453341876, 5.68285466581244, -11.3171453341876, -2.31714533418756, 6.68285466581244, -16.3171453341876, 8.38722300809366, 42.3872230080937, 13.3872230080937, 27.8866653386156, -25.1133346613844, 7.88666533861561, -21.1133346613844, 9.71094990017841, 5.71094990017841, 26.7109499001784, -7.28905009982159, 21.7109499001784, -20.2890500998216, 6.226070726676, -15.773929273324, -28.773929273324, 14.226070726676, -14.773929273324, 21.226070726676, 6.226070726676, 29.226070726676, 13.226070726676, -1.18678877265756, 15.8132112273424, 1.81321122734244, 25.8132112273424, -0.186788772657565, 3.81321122734244, -10.1867887726576, 15.8132112273424, 9.81321122734244, 9.81321122734244, -35.0551967576179, 14.9448032423821, 13.9448032423821, -17.0551967576179, -6.05519675761792, -17.7296046985831, 14.9139035439664), gradient = structure(c(0, 0, 0, 0, 0, 56.989995924654, 56.989995924654, 94.3649041101607, 94.3649041101607, 94.3649041101607, 94.3649041101607, 94.3649041101607, 94.3649041101607, 109.608811230383, 109.608811230383, 109.608811230383, 107.478028232287, 107.478028232287, 107.478028232287, 107.478028232287, 94.6057793667664, 94.6057793667664, 94.6057793667664, 94.6057793667664, 94.6057793667664, 94.6057793667664, 76.6771074226725, 76.6771074226725, 76.6771074226725, 76.6771074226725, 76.6771074226725, 76.6771074226725, 76.6771074226725, 76.6771074226725, 76.6771074226725, 57.5975949121373, 57.5975949121373, 57.5975949121373, 57.5975949121373, 57.5975949121373, 57.5975949121373, 57.5975949121373, 57.5975949121373, 57.5975949121373, 57.5975949121373, 39.6403646307366, 39.6403646307366, 39.6403646307366, 39.6403646307366, 39.6403646307366, 10.7055301785859, 0, 1.00000000551046, 1.00000000551046, 1.00000000551046, 1.00000000551046, 1.00000000551046, 0.914597467778369, 0.914597467778369, 0.764820801027804, 0.764820801027804, 0.764820801027804, 0.764820801027804, 0.764820801027804, 0.764820801027804, 0.599195286063472, 0.599195286063472, 0.599195286063472, 0.446659102876937, 0.446659102876937, 0.446659102876937, 0.446659102876937, 0.319471715663991, 0.319471715663991, 0.319471715663991, 0.319471715663991, 0.319471715663991, 0.319471715663991, 0.21965732107982, 0.21965732107982, 0.21965732107982, 0.21965732107982, 0.21965732107982, 0.21965732107982, 0.21965732107982, 0.21965732107982, 0.21965732107982, 0.144322069921372, 0.144322069921372, 0.144322069921372, 0.144322069921372, 0.144322069921372, 0.144322069921372, 0.144322069921372, 0.144322069921372, 0.144322069921372, 0.144322069921372, 0.0889140940358009, 0.0889140940358009, 0.0889140940358009, 0.0889140940358009, 0.0889140940358009, 0.0202635232425103, 2.60032456603692e-08, 0, 0, 0, 0, 0, 0.165626203544259, 0.165626203544259, 0.341691261149167, 0.341691261149167, 0.341691261149167, 0.341691261149167, 0.341691261149167, 0.341691261149167, 0.503396799290371, 0.503396799290371, 0.503396799290371, 0.638987326722699, 0.638987326722699, 0.638987326722699, 0.638987326722699, 0.746106779008021, 0.746106779008021, 0.746106779008021, 0.746106779008021, 0.746106779008021, 0.746106779008021, 0.827421615259225, 0.827421615259225, 0.827421615259225, 0.827421615259225, 0.827421615259225, 0.827421615259225, 0.827421615259225, 0.827421615259225, 0.827421615259225, 0.887496120452751, 0.887496120452751, 0.887496120452751, 0.887496120452751, 0.887496120452751, 0.887496120452751, 0.887496120452751, 0.887496120452751, 0.887496120452751, 0.887496120452751, 0.931061257482989, 0.931061257482989, 0.931061257482989, 0.931061257482989, 0.931061257482989, 0.984387422945875, 0.999999996451695), .Dim = c(52L, 3L))), c(52L, 1L), NULL); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray23() {
        assertEval("argv <- list(c(1L, 2L, 1L), 3L, structure(list(c('1', '2', NA)), .Names = '')); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray24() {
        assertEval("argv <- list(c(4L, 10L, 16L, 22L, 28L, 34L, 40L, 46L, 52L, 58L, 64L, 70L, 76L, 82L, 88L, 94L, 100L, 106L, 112L, 118L), 4:5, list(NULL, c('V5', 'V6', 'V7', 'V8', 'V9'))); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray25() {
        assertEval("argv <- list(c(1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 11, 11, 11, 11, 11, 12, 12, 12, 12, 12, 13, 13, 13, 13, 13, 14, 14, 14, 14, 14, 15, 15, 15, 15, 15, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 18, 18, 18, 18, 18, 19, 19, 19, 19, 19, 20, 20, 20, 20, 20, 21, 21, 21, 21, 21, 22, 22, 22, 22, 22, 23, 23, 23, 23, 23, 24, 24, 24, 24, 24, 25, 25, 25, 25, 25, 26, 26, 26, 26, 26, 27, 27, 27, 27, 27, 28, 28, 28, 28, 28, 29, 29, 29, 29, 29, 30, 30, 30, 30, 30, 31, 31, 31, 31, 31, 32, 32, 32, 32, 32, 33, 33, 33, 33, 33, 34, 34, 34, 34, 34, 35, 35, 35, 35, 35, 36, 36, 36, 36, 36, 37, 37, 37, 37, 37, 38, 38, 38, 38, 38, 39, 39, 39, 39, 39, 40, 40, 40, 40, 40, 41, 41, 41, 41, 41, 42, 42, 42, 42, 42, 43, 43, 43, 43, 43, 44, 44, 44, 44, 44, 45, 45, 45, 45, 45, 46, 46, 46, 46, 46, 47, 47, 47, 47, 47, 48, 48, 48, 48, 48, 49, 49, 49, 49, 49, 50, 50, 50, 50, 50, 51, 51, 51, 51, 51, 52, 52, 52, 52, 52, 53, 53, 53, 53, 53, 54, 54, 54, 54, 54, 55, 55, 55, 55, 55, 56, 56, 56, 56, 56, 57, 57, 57, 57, 57, 58, 58, 58, 58, 58, 59, 59, 59, 59, 59, 60, 60, 60, 60, 60, 61, 61, 61, 61, 61, 62, 62, 62, 62, 62, 63, 63, 63, 63, 63, 64, 64, 64, 64, 64, 65, 65, 65, 65, 65, 66, 66, 66, 66, 66, 67, 67, 67, 67, 67, 68, 68, 68, 68, 68, 69, 69, 69, 69, 69, 70, 70, 70, 70, 70, 71, 71, 71, 71, 71, 72, 72, 72, 72, 72, 73, 73, 73, 73, 73, 74, 74, 74, 74, 74, 75, 75, 75, 75, 75, 76, 76, 76, 76, 76, 77, 77, 77, 77, 77, 78, 78, 78, 78, 78, 79, 79, 79, 79, 79, 80, 80, 80, 80, 80, 81, 81, 81, 81, 81, 82, 82, 82, 82, 82, 83, 83, 83, 83, 83, 84, 84, 84, 84, 84, 85, 85, 85, 85, 85, 86, 86, 86, 86, 86, 87, 87, 87, 87, 87, 88, 88, 88, 88, 88, 89, 89, 89, 89, 89, 90, 90, 90, 90, 90, 91, 91, 91, 91, 91, 92, 92, 92, 92, 92, 93, 93, 93, 93, 93, 94, 94, 94, 94, 94, 95, 95, 95, 95, 95, 96, 96, 96, 96, 96, 97, 97, 97, 97, 97, 98, 98, 98, 98, 98, 99, 99, 99, 99, 99, 100, 100, 100, 100, 100, 101, 101, 101, 101, 101, 102, 102, 102, 102, 102, 103, 103, 103, 103, 103, 104, 104, 104, 104, 104, 105, 105, 105, 105, 105, 106, 106, 106, 106, 106, 107, 107, 107, 107, 107, 108, 108, 108, 108, 108, 109, 109, 109, 109, 109, 110, 110, 110, 110, 110, 111, 111, 111, 111, 111, 112, 112, 112, 112, 112, 113, 113, 113, 113, 113, 114, 114, 114, 114, 114, 115, 115, 115, 115, 115, 116, 116, 116, 116, 116, 117, 117, 117, 117, 117, 118, 118, 118, 118, 118, 119, 119, 119, 119, 119, 120, 120, 120, 120, 120), c(5, 2, 3, 4, 5), list(NULL, NULL, c('a', 'b', 'c'), NULL, c('V5', 'V6', 'V7', 'V8', 'V9'))); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray26() {
        assertEval("argv <- list('', c(4L, 3L), list(c('<none>', 'Hair:Eye', 'Hair:Sex', 'Eye:Sex'), c('Df', 'Deviance', 'AIC'))); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testarray27() {
        assertEval("argv <- list(-1, c(3L, 2L), list(c('a', 'b', 'c'), NULL)); .Internal(array(argv[[1]], argv[[2]], argv[[3]]))");
    }

    @Test
    public void testArray() {
        assertEval(Output.MayIgnoreWarningContext, "{ array(1:4, 1:2, 4) }");
        assertEval(Output.MayIgnoreWarningContext, "{ array(1:4, c(1+2i, 2+2i)) }");
        assertEval("{ array(as.raw(1:4)) }");
        assertEval("{ array(1:4, integer()) }");
        assertEval("{ array(NULL) }");
        assertEval("{ array(NA) }");
        assertEval("{ array(1:4, NULL) }");
        assertEval("{ .Internal(array(NULL, 1, NULL)) }");
        assertEval("{ .Internal(array(NA, 1, NULL)) }");
        assertEval("{ f<-function() 42; .Internal(array(f, 1, NULL)) }");
    }

}
