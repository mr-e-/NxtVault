package com.nxt.nxtvault.util;

import java.util.Calendar;
import java.util.TimeZone;

public final class Nxt {

    public static final int BLOCK_HEADER_LENGTH = 232;
    public static final int MAX_NUMBER_OF_TRANSACTIONS = 255;
    public static final int MAX_PAYLOAD_LENGTH = MAX_NUMBER_OF_TRANSACTIONS * 176;
    public static final long MAX_BALANCE_NXT = 1000000000;
    public static final long ONE_NXT = 100000000;
    public static final long MAX_BALANCE_NQT = MAX_BALANCE_NXT * ONE_NXT;
    public static final long INITIAL_BASE_TARGET = 153722867;
    public static final long MAX_BASE_TARGET = MAX_BALANCE_NXT * INITIAL_BASE_TARGET;
    public static final int MAX_ROLLBACK = 1440;
    static {
        if (MAX_ROLLBACK < 1440) {
            throw new RuntimeException("nxt.maxRollback must be at least 1440");
        }
    }

    public static final int MAX_ALIAS_URI_LENGTH = 1000;
    public static final int MAX_ALIAS_LENGTH = 100;

    public static final int MAX_ARBITRARY_MESSAGE_LENGTH = 1000;
    public static final int MAX_ENCRYPTED_MESSAGE_LENGTH = 1000;

    public static final int MAX_ACCOUNT_NAME_LENGTH = 100;
    public static final int MAX_ACCOUNT_DESCRIPTION_LENGTH = 1000;

    public static final long MAX_ASSET_QUANTITY_QNT = 1000000000L * 100000000L;
    public static final int MIN_ASSET_NAME_LENGTH = 3;
    public static final int MAX_ASSET_NAME_LENGTH = 10;
    public static final int MAX_ASSET_DESCRIPTION_LENGTH = 1000;
    public static final int MAX_ASSET_TRANSFER_COMMENT_LENGTH = 1000;

    public static final int MAX_POLL_NAME_LENGTH = 100;
    public static final int MAX_POLL_DESCRIPTION_LENGTH = 1000;
    public static final int MAX_POLL_OPTION_LENGTH = 100;
    public static final int MAX_POLL_OPTION_COUNT = 100;

    public static final int MAX_DGS_LISTING_QUANTITY = 1000000000;
    public static final int MAX_DGS_LISTING_NAME_LENGTH = 100;
    public static final int MAX_DGS_LISTING_DESCRIPTION_LENGTH = 1000;
    public static final int MAX_DGS_LISTING_TAGS_LENGTH = 100;
    public static final int MAX_DGS_GOODS_LENGTH = 10240;

    public static final int MAX_HUB_ANNOUNCEMENT_URIS = 100;
    public static final int MAX_HUB_ANNOUNCEMENT_URI_LENGTH = 1000;
    public static final long MIN_HUB_EFFECTIVE_BALANCE = 100000;

    public static final boolean isTestnet = false;
    public static final boolean isOffline = false;

    public static final int ALIAS_SYSTEM_BLOCK = 22000;
    public static final int TRANSPARENT_FORGING_BLOCK = 30000;
    public static final int ARBITRARY_MESSAGES_BLOCK = 40000;
    public static final int TRANSPARENT_FORGING_BLOCK_2 = 47000;
    public static final int TRANSPARENT_FORGING_BLOCK_3 = 51000;
    public static final int TRANSPARENT_FORGING_BLOCK_4 = 64000;
    public static final int TRANSPARENT_FORGING_BLOCK_5 = 67000;
    public static final int TRANSPARENT_FORGING_BLOCK_6 = isTestnet ? 75000 : 130000;
    public static final int TRANSPARENT_FORGING_BLOCK_7 = Integer.MAX_VALUE;
    public static final int TRANSPARENT_FORGING_BLOCK_8 = isTestnet ? 78000 : 215000;
    public static final int NQT_BLOCK = isTestnet ? 76500 : 132000;
    public static final int FRACTIONAL_BLOCK = isTestnet ? NQT_BLOCK : 134000;
    public static final int ASSET_EXCHANGE_BLOCK = isTestnet ? NQT_BLOCK : 135000;
    public static final int REFERENCED_TRANSACTION_FULL_HASH_BLOCK = isTestnet ? NQT_BLOCK : 140000;
    public static final int REFERENCED_TRANSACTION_FULL_HASH_BLOCK_TIMESTAMP = isTestnet ? 13031352 : 15134204;
    public static final int VOTING_SYSTEM_BLOCK = Integer.MAX_VALUE;
    public static final int DIGITAL_GOODS_STORE_BLOCK = isTestnet ? 77341 : 213000;
    public static final int PUBLIC_KEY_ANNOUNCEMENT_BLOCK = isTestnet ? 77341 : 215000;
    public static final int LAST_KNOWN_BLOCK = isTestnet ? 80000 : 258000;

    //2 Phased Voting
    public static final byte MAX_VOTES_PER_VOTING_TRANSACTION = 2;
    public static final byte VOTING_MODEL_BALANCE = 0;
    public static final byte VOTING_MODEL_ACCOUNT = 1;
    public static final byte VOTING_MODEL_ASSET = 2;
    public static final byte VOTING_MODEL_MS_COIN = 3;

    public static final byte VOTING_DEFAULT_MIN_BALANCE = 0;
    public static final byte VOTING_DEFAULT_MIN_NUMBER_OF_CHOICES = 1;
    public static final byte VOTING_MIN_RANGE_VALUE_LIMIT = -100;
    public static final byte VOTING_MAX_RANGE_VALUE_LIMIT = 100;
    public static final byte VOTING_NO_VOTE_VALUE = Byte.MIN_VALUE;
    public static final byte VOTING_MIN_VOTE_DURATION = 10;

    public static final byte PENDING_TRANSACTIONS_MAX_WHITELIST_SIZE = 10;
    public static final byte PENDING_TRANSACTIONS_MAX_BLACKLIST_SIZE = 5;
    public static final int  PENDING_TRANSACTIONS_MAX_PERIOD = 14*1440;

    static final long UNCONFIRMED_POOL_DEPOSIT_NQT = (isTestnet ? 50 : 100) * ONE_NXT;

    public static final long EPOCH_BEGINNING;
    static {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.YEAR, 2013);
        calendar.set(Calendar.MONTH, Calendar.NOVEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 24);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        EPOCH_BEGINNING = calendar.getTimeInMillis();
    }

    public static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz";

    public static final int EC_RULE_TERMINATOR = 600; /* cfb: This constant defines a straight edge when "longest chain"
                                                        rule is outweighed by "economic majority" rule; the terminator
                                                        is set as number of seconds before the current time. */

    public static final int EC_BLOCK_DISTANCE_LIMIT = 60;
    //home
    public static final String API_HOST = "192.168.0.104";
    //work
    //public static final String API_HOST = "10.0.12.98";

    public static final int API_PORT = 6876;

    public static final String TEST_ACCOUNT_PASSPHRASE = "loser puzzle need adore safe hop swim whether dad edge hate cold";

    //pale blame bus dot strike egg yard delicate nation stretch less girl
    private Nxt() {} // never
}
