package ru.akvine.marketspace.bot.services.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.marketspace.bot.entities.AdvertStatisticEntity;
import ru.akvine.marketspace.bot.services.domain.base.SoftBean;

@Data
@Accessors(chain = true)
public class AdvertStatisticBean extends SoftBean {
    private Long id;
    private String views;
    private String clicks;
    private String ctr;
    private String cpc;
    private String sum;
    private String atbs;
    private String orders;
    private String cr;
    private String shks;
    private String sumPrice;
    private AdvertBean advertBean;

    public AdvertStatisticBean(AdvertStatisticEntity advertStatisticEntity) {
        this.id = advertStatisticEntity.getId();
        this.views = advertStatisticEntity.getViews();
        this.clicks = advertStatisticEntity.getClicks();
        this.ctr = advertStatisticEntity.getCtr();
        this.cpc = advertStatisticEntity.getCpc();
        this.sum = advertStatisticEntity.getSum();
        this.atbs = advertStatisticEntity.getAtbs();
        this.orders = advertStatisticEntity.getOrders();
        this.cr = advertStatisticEntity.getCr();
        this.shks = advertStatisticEntity.getShks();
        this.sumPrice = advertStatisticEntity.getSumPrice();
        this.advertBean = new AdvertBean(advertStatisticEntity.getAdvertEntity());

        this.createdDate = advertStatisticEntity.getCreatedDate();
        this.updatedDate = advertStatisticEntity.getUpdatedDate();
        this.deletedDate = advertStatisticEntity.getDeletedDate();
        this.deleted = advertStatisticEntity.isDeleted();
    }
}
