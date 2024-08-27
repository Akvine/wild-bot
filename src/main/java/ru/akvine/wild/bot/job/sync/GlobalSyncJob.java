package ru.akvine.wild.bot.job.sync;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import ru.akvine.wild.bot.constants.MDCConstants;

@Slf4j
public class GlobalSyncJob {
    private final SyncCardTypeJob cardTypeJob;
    private final SyncCardJob cardJob;
    private final SyncAdvertJob advertJob;

    private final String mdcName;
    private final String mdcChatId;

    @Value("${sync.card.types.enabled}")
    private boolean syncCardTypesEnabled;
    @Value("${sync.card.enabled}")
    private boolean syncCardsEnabled;
    @Value("${sync.adverts.enabled}")
    private boolean syncAdvertsEnabled;

    public GlobalSyncJob(SyncCardTypeJob cardTypeJob,
                         SyncCardJob cardJob,
                         SyncAdvertJob advertJob,
                         String mdcName,
                         String mdcChatId) {
        this.cardTypeJob = cardTypeJob;
        this.cardJob = cardJob;
        this.advertJob = advertJob;
        this.mdcName = mdcName;
        this.mdcChatId = mdcChatId;
    }

    @Scheduled(fixedDelayString = "${global.sync.cron.milliseconds}")
    public void globalSync() {
        MDC.put(MDCConstants.USERNAME, mdcName);
        MDC.put(MDCConstants.CHAT_ID, mdcChatId);

        if (syncCardTypesEnabled) {
            cardTypeJob.sync();
        }
        if (syncCardsEnabled) {
            cardJob.sync();
        }
        if (syncAdvertsEnabled) {
            advertJob.sync();
        }

        MDC.clear();
    }
}
