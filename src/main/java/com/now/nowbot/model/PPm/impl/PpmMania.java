package com.now.nowbot.model.PPm.impl;

import com.now.nowbot.config.NowbotConfig;
import com.now.nowbot.model.JsonData.BpInfo;
import com.now.nowbot.model.JsonData.OsuUser;
import com.now.nowbot.model.PPm.Ppm;
import com.now.nowbot.util.Panel.PanelBuilder;
import com.now.nowbot.util.PanelUtil;
import com.now.nowbot.util.SkiaImageUtil;
import org.jetbrains.skija.Image;
import org.jetbrains.skija.Paint;
import org.springframework.lang.Nullable;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.functions.Func4;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class PpmMania extends Ppm {
    public PpmMania(OsuUser user, List<BpInfo> bps){
        double [] allBpPP = new double[bps.size()];
        for (int i = 0; i < bps.size(); i++) {
            var bp = bps.get(i);
            bpp += bp.getWeight().getPp();
            allBpPP[i] += Math.log10(bp.getWeight().getPp())/2;

            switch (bp.getRank()){
                case "XH", "X" -> xx++;
                case "SH", "S" -> xs++;
                case "A" -> xa++;
                case "B" -> xb++;
                case "C" -> xc++;
                case "D" -> xd++;
            }
            if (!bp.isPerfect()) notfc ++;
            if(i < 10){
                ppv0 += bp.getPp();
                accv0 += bp.getAccuracy();
                lengv0 += bp.getBeatmap().getTotalLength();
                pgr0 += 1.0*bp.getStatistics().getCountGeki()/bp.getStatistics().getCount300();
            }else if(i>=45 && i<55){
                ppv45 += bp.getPp();
                accv45 += bp.getAccuracy();
                lengv45 += bp.getBeatmap().getTotalLength();
                pgr45 += 1.0*bp.getStatistics().getCountGeki()/bp.getStatistics().getCount300();
            }else if(i>=90){
                ppv90 += bp.getPp();
                accv90 += bp.getAccuracy();
                lengv90 += bp.getBeatmap().getTotalLength();
                pgr90 += 1.0*bp.getStatistics().getCountGeki()/bp.getStatistics().getCount300();
            }
        }
        bonus = bonusPP(allBpPP, user.getStatustucs().getPlayCount());
        rawpp = bpp + bonus;user.getStatustucs().getPp();

        ppv0 /= 10;
        ppv45 /= 10;
        ppv90 /= 10;
        accv0 /= 10;
        accv45 /= 10;
        accv90 /= 10;
        lengv0 /= 10;
        lengv45 /= 10;
        lengv90 /= 10;
        pgr0 /= 10;
        pgr45 /= 10;
        pgr90 /= 10;
        if (bps.size()<90) {
            ppv90 = 0; accv90 = 0; lengv90 = 0; pgr0 = 0;
        }
        if (bps.size()<45) {
            ppv45 = 0; accv45 = 0; lengv45 = 0; pgr45 = 0;
        }
        if (bps.size()<10) {
            ppv0 = 0; accv0 = 0; lengv0 = 0; pgr90 = 0;
        }
        double pp = user.getStatustucs().getPp();
        double acc = user.getStatustucs().getAccuracy();
        double pc = user.getStatustucs().getPlayCount();
        double pt = user.getStatustucs().getPlayTime();
        double tth = user.getStatustucs().getTotalHits();


        // 4.1 ??????fACC formulaic accuracy 0-1.2
        {
            double rFA;
            if (acc >= 97) {
                rFA = 1;
            } else if (acc >= 60) {
                rFA = Math.pow((acc / 100 - 0.6) / 0.37D, 2.567D);
            } else {
                rFA = 0;
            }

            double VAB;
            if (acc / 100 >= 1) {
                VAB = 0.2;
            } else if (acc / 100 >= 0.97) {
                VAB = 0.2 * Math.pow((acc / 100 - 0.97) / 3D, 0.2D);
            } else {
                VAB = 0;
            }

            value1 = rFA + VAB;
            value1 = check(value1, 0, 1.2);
        }
        // 4.2 ??????PTT potential 0-1.2
        {
            double rBPV = ppv0 / (ppv90 + 5);
            double rBPD = ppv0 == 0 ? 0 : (rawpp / ppv0);
            double LPI = pp > 1000 ? 1 : Math.pow(pp / 1000D, 0.5D); // low PP index ???pp?????? ??????PP?????????ptt??????????????????????????????

            double BPD; // BP density BP??????
            if (rBPD == 0) {
                BPD = 0;
            } else if (rBPD >= 20) {
                BPD = 0;
            } else if (rBPD >= 19) {
                BPD = (20 - rBPD) * 0.6D;
            } else if (rBPD >= 15) {
                BPD = (19 - rBPD) * 0.1D + 0.6D;
            } else {
                BPD = 1;
            }

            double BPV; // BP vitality BP??????
            if (rBPV >= 1.4) {
                BPV = 1;
            } else if (rBPV >= 1.2) {
                BPV = (rBPV - 1.2) * 2D + 0.6D;
            } else if (rBPV >= 1.0) {
                BPV = (rBPV - 1.0) * 3D;
            } else {
                BPV = 0;
            }

            double VWB; // very wide (bp) bonus ???????????????
            if (rBPV >= 6.8) {
                VWB = 0.2;
            } else if (rBPV >= 1.8) {
                VWB = (rBPV - 1.8) / 25D;
            } else {
                VWB = 0;
            }

            value2 = Math.pow(BPD, 0.4D) * 0.2D + BPV * 0.8D * LPI + VWB;
            value2 = check(value2, 0, 1.2);
        }

        // 4.3 ??????STA stamina 0-1.2
        {
            double rSPT = pc == 0 ? 0 : (pt / pc);
            double SPT; // single play count time ??????????????????
            if (rSPT >= 120) {
                SPT = 1;
            } else if (rSPT >= 100) {
                SPT = (rSPT - 100) * 0.005D + 0.9D;
            } else if (rSPT >= 60) {
                SPT = (rSPT - 60) * 0.0075D + 0.6D;
            } else if (rSPT >= 40) {
                SPT = (rSPT - 40) * 0.03D;
            } else {
                SPT = 0;
            }

            double rBPT = lengv0 * 0.7 + lengv45 * 0.2 + lengv90 * 0.1; // BP playtime BP ????????????

            double BPT; // BP playtime BP ???????????? ???????????????fLENT???
            if (rBPT >= 260) {
                BPT = 1;
            } else if (rBPT >= 220) {
                BPT = (rBPT - 220) * 0.0025D + 0.9D;
            } else if (rBPT >= 140) {
                BPT = (rBPT - 140) * 0.00375D + 0.6D;
            } else if (rBPT >= 100) {
                BPT = (rBPT - 100) * 0.015D;
            } else {
                BPT = 0;
            }

            double VLB; // very long bonus ????????????
            if (rBPT >= 320) {
                VLB = 0.2;
            } else if (rBPT >= 280) {
                VLB = (rBPT - 280) * 0.005D;
            } else {
                VLB = 0;
            }

            value3 = Math.pow((SPT * 0.4D + BPT * 0.6D), 0.8D) + VLB;
            value3 = check(value3, 0, 1.2);
        }

        // 4.4 ??????PRE precision 0-1.2 ???????????????????????????STB ??? mania ????????????????????????
        {
            double rPGR = pgr0 * 0.7 + pgr45 * 0.2 + pgr90 * 0.1; // raw perfect-great rate ???????????????

            double PGR; // perfect-great rate ?????????/????????????
            if (rPGR >= 2.2) {
                PGR = 1;
            } else if (rPGR >= 1.0) {
                PGR = (rPGR - 1.0) / 1.2D;
            } else {
                PGR = 0;
            }

            double VPB; // very precise bonus ???????????????
            if (rPGR >= 3) {
                VPB = 0.2;
            } else if (rPGR >= 2.2) {
                VPB = (rPGR - 2.2) / 4D;
            } else {
                VPB = 0;
            }

            value4 = Math.pow(PGR, 0.4D) + VPB;
            value4 = check(value4, 0, 1.2);
        }

        // 4.5 ??????ENG energy 0-1.2
        {
            double rLNT = Math.log1p(tth);
            double LNT; // LNTTH ???????????????????????????
            if (rLNT >= 17) {
                LNT = 1;
            } else if (rLNT >= 12) {
                LNT = (rLNT - 12) * 0.2D;
            } else {
                LNT = 0;
            }

            double VEB; // very energetic bonus ???????????????
            if (rLNT >= 18) {
                VEB = 0.2;
            } else if (rLNT >= 17) {
                VEB = (rLNT - 17) * 0.2D;
            } else {
                VEB = 0;
            }

            value5 = Math.pow(LNT, 0.6D) + VEB;
            value5 = check(value5, 0, 1.2);
        }

        // 4.6 ??????STH strength 0-1.2
        {
            double rHPS = pt == 0 ? 0 : tth / pt; // raw hit per second ??????????????????

            double rLNB = Math.log1p(ppv0 * lengv0);
            // raw ln (the) best (performance multiplayer) ????????????????????????????????????

            double HPS;
            if (rHPS >= 18) {
                HPS = 1;
            } else if (rHPS >= 0) {
                HPS = rHPS / 18D;
            } else {
                HPS = 0;
            }

            double LNB;
            if (rLNB >= 11.5) {
                LNB = 1;
            } else if (rLNB >= 0) {
                LNB = Math.pow(rLNB / 11.5D, 3.0D);
            } else {
                LNB = 0;
            }

            double VHB; // very high (pp) bonus ???????????????
            if (rLNB >= 12.5) {
                VHB = 0.2;
            } else if (rLNB >= 11.5) {
                VHB = Math.pow(rLNB - 11.5, 0.5D) * 0.2D;
            } else {
                VHB = 0;
            }

            value6 = Math.pow(HPS * 0.2 + LNB * 0.8, 0.4D) + VHB;
            value6 = check(value6, 0, 1.2);
        }

        // 4.7 ??????TTL Total / Overall 0-1.2
        value7 = value1 * 0.2 + value2 * 0.1 + value3 * 0.2 + value4 * 0.25 + value5 * 0.05 + value6 * 0.2;
        value7 *= 100;

        // 4.8 ??????SAN sanity 0-1.2
        {
            double LPI = pp > 1000 ? 1 : Math.pow(pp / 1000D, 0.5D); // low PP index ???pp?????? ??????PP?????????rSAN??????????????????????????????

            double PCI = Math.pow(ppv0 * 30 / (pc + 100), 0.8D); // play count index PC??????

            double rSAN = value1 * value2 * Math.sqrt(Math.pow(ppv0, 2.0D) / ((ppv45 + 1.0) * (ppv90 + 1.0))) * LPI * PCI; // raw sanity ????????????

            if (rSAN >= 5) {
                value8 = 3D / rSAN;
            } else if (rSAN >= 1) {
                value8 = 1.1 - rSAN * 0.1D;
            } else {
                value8 = 1.2 - rSAN * 0.2D;
            }
            value8 = check(value8, 0, 1.2);
            value8 *= 100;
        }
    }

    @Override
    public void drawOverImage(Function<Image, PanelBuilder> doAct, @Nullable Image userImg) {
        if (userImg != null) {
            doAct.apply(userImg);
            return;
        }
        try {
            doAct.apply(SkiaImageUtil.getImage(NowbotConfig.BG_PATH + "ExportFileV3/overlay-ppminus-maniaSP.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drawValueName(Func4<Integer, String, String, Paint, PanelBuilder> doAct) {
        var p = new Paint().setARGB(255,161,161,161);
        doAct.call(0, "FAC",null,p);
        doAct.call(1, "PTT",null,p);
        doAct.call(2, "STA",null,p);
        doAct.call(3, "PRE",null,p);
        doAct.call(4, "ENG",null,p);
        doAct.call(5, "STH",null,p);
    }

    @Override
    public void drawValue(Func3<Integer, String, String, PanelBuilder> doAct) {
        doAct.call(0, String.valueOf((int) (value1 * 100)), PanelUtil.cutDecimalPoint(value1 * 100)/*<-????????????*/);
        doAct.call(1, String.valueOf((int) (value2 * 100)), PanelUtil.cutDecimalPoint(value2 * 100)/*<-????????????*/);
        doAct.call(2, String.valueOf((int) (value3 * 100)), PanelUtil.cutDecimalPoint(value3 * 100)/*<-????????????*/);
        doAct.call(3, String.valueOf((int) (value4 * 100)), PanelUtil.cutDecimalPoint(value4 * 100)/*<-????????????*/);
        doAct.call(4, String.valueOf((int) (value5 * 100)), PanelUtil.cutDecimalPoint(value5 * 100)/*<-????????????*/);
        doAct.call(5, String.valueOf((int) (value6 * 100)), PanelUtil.cutDecimalPoint(value6 * 100)/*<-????????????*/);
    }

    @Override
    public void drawRank(Func2<Integer, Double, PanelBuilder> doAct) {
        doAct.call(0, value1);
        doAct.call(1, value2);
        doAct.call(2, value3);
        doAct.call(3, value4);
        doAct.call(4, value5);
        doAct.call(5, value6);
    }

    @Override
    public void drawTotleName(Function<String, PanelBuilder> left, Function<String, PanelBuilder> right) {
        left.apply("Overall");
        right.apply("Sanity");
    }

    @Override
    public void drawTotleValue(Func2<String, String, PanelBuilder> left, Func2<String, String, PanelBuilder> right) {
        left.call(String.valueOf((int) value7), PanelUtil.cutDecimalPoint(value7));
        right.call(String.valueOf((int) value8), PanelUtil.cutDecimalPoint(value8));
    }
}
