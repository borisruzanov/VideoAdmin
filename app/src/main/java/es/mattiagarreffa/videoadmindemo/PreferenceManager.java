package es.mattiagarreffa.videoadmindemo;

public class PreferenceManager {
    static String accountNamem;

    public  static String getAccountName() {
        return accountNamem;
    }

    public static void setAccountName (String accountName)  {
        accountNamem = accountName;
    }
}
