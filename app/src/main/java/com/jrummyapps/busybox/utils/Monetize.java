/*
 * Copyright (C) 2017 JRummy Apps Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jrummyapps.busybox.utils;

import com.jrummyapps.android.app.App;
import com.jrummyapps.android.obfuscate.LegacyObfuscate;
import com.jrummyapps.android.prefs.Prefs;

public class Monetize {

  public static final String ENCRYPTED_LICENSE_KEY =
      "X0^8N]^9N0ß2J1£R©P£«|V^$WL8B00#PN0EMNCE|(LE4T0^2N1IHNCE||0ERS}S1W0J©#&8VT®J}4^I4X¥J~NCJEX#|AW$#×)&|WW]JQIP4ATCQC#]RNJ&Q1IXJ4JP#X4O#~IXS%4Z59#Z{Q0O8JIVS$WO5%4O4^42£2N#«©©#4JX}JLS]|LW0N{J$J8~1{R6®8OW1JZ)^J4©®LV6&8À6&ß÷0}«}H$0AW0K!0V[ATV#ÀZ¥BGSCßJI]4£40ÀC~C^CJ0ß||OB«4}|ßX18QZENC©L5K6P{£)1RAZ#JAI&4}|#JKZ##ÀHCNC0#I÷4®U1À#«EN®^×J0E«S×5QW{5ßTLNVW×B÷#]8|N0LRXPß©N×#J|1RP~{I~S®^1(#B&T×4{I@£^~V|[|×016L44T]J÷TZÀ¢~ADE|]ß&N1I#W]]A6&I@JPRP#XI14#4CT^«¢©]L$J#WVÀO8L00z1)×JL|X5^I{#N6^S{|&0!4¥#G©LK1TP4«SZIÀJLJ¢W&DZ4L{«Z¥4G©$Nß|OI8|LE|N0[¯";

  public static final String ENCRYPTED_REMOVE_ADS_PRODUCT_ID = "S&#£)$J^O1E]S!¯¯";

  public static String decrypt(String value) {
    return LegacyObfuscate.getInstance().decrypt(value);
  }

  public static void removeAds() {
    Prefs.getInstance().save(ENCRYPTED_REMOVE_ADS_PRODUCT_ID, true);
  }

  public static boolean isAdsRemoved() {
    boolean defValue = App.getContext().getPackageName().endsWith("pro");
    return Prefs.getInstance().get(ENCRYPTED_REMOVE_ADS_PRODUCT_ID, defValue);
  }

  public static final class Event {

    public static final class OnAdsRemovedEvent {
    }

    public static final class OnPurchasedPremiumEvent {
    }

    public static final class RequestInterstitialAd {
    }

    public static final class RequestRemoveAds {
    }

  }

}
