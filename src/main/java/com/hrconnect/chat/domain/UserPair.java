package com.hrconnect.chat.domain;

import java.util.UUID;

/**
 * Cap hai nguoi dung da sap xep theo dung thu tu ma PostgreSQL dung de so sanh uuid.
 *
 * <p>Luu y: {@link UUID#compareTo(UUID)} cua Java so sanh co dau (signed) nen KHONG khop voi
 * PostgreSQL (so sanh tung byte khong dau). Vi vay o day dung {@link Long#compareUnsigned}.
 * Neu dung compareTo, mot so cap id se vi pham CHECK (user_a_id &lt; user_b_id).
 */
public record UserPair(UUID userA, UUID userB) {

    public UserPair {
        if (compareLikePostgres(userA, userB) >= 0) {
            throw new IllegalArgumentException("userA phai nho hon userB");
        }
    }

    public static UserPair of(UUID x, UUID y) {
        int cmp = compareLikePostgres(x, y);
        if (cmp == 0) {
            throw new IllegalArgumentException("Hai user phai khac nhau");
        }
        return cmp < 0 ? new UserPair(x, y) : new UserPair(y, x);
    }

    public static int compareLikePostgres(UUID x, UUID y) {
        int hi = Long.compareUnsigned(x.getMostSignificantBits(), y.getMostSignificantBits());
        return hi != 0 ? hi : Long.compareUnsigned(x.getLeastSignificantBits(), y.getLeastSignificantBits());
    }
}
