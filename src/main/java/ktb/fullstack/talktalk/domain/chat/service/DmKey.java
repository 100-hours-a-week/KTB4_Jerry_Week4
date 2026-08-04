package ktb.fullstack.talktalk.domain.chat.service;

public final class DmKey {

    private DmKey() {}

    public static String of(Long userIdA, Long userIdB) {
        long low = Math.min(userIdA, userIdB);
        long high = Math.max(userIdA, userIdB);
        return low + ":" + high;
    }
}
