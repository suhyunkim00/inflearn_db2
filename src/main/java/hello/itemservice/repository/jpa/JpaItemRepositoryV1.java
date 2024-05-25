package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;


@Slf4j
@Repository
@Transactional //데이터 변경할 때 무조건 필요
public class JpaItemRepositoryV1 implements ItemRepository {

    private final EntityManager em; //의존관계 주입
    public JpaItemRepositoryV1(EntityManager em) {
        this.em = em;
    }
    @Override
    public Item save(Item item) {
        em.persist(item); //아이템 저장
        return item;
    }
    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = em.find(Item.class, itemId); //em.find(type, pk)
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity()); //트랜잭션이 커밋되는 시점에 업데이트쿼리 만들어서 자동으로 DB에 날림
    }
    @Override
    public Optional<Item> findById(Long id) {
        Item item = em.find(Item.class, id); //em.find(type, pk)
        return Optional.ofNullable(item); //null일 수 있으므로
    }
    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String jpql = "select i from Item i"; //jpql 문법은 테이블 대상이 아닌 아이템 엔티티 대상으로 함, 동적쿼리에 약함

        Integer maxPrice = cond.getMaxPrice();
        String itemName = cond.getItemName();
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            jpql += " where";
        }
        boolean andFlag = false;
        if (StringUtils.hasText(itemName)) {
            jpql += " i.itemName like concat('%',:itemName,'%')";
            andFlag = true;
        }
        if (maxPrice != null) {
            if (andFlag) {
                jpql += " and";
            }
            jpql += " i.price <= :maxPrice";
        }
        log.info("jpql={}", jpql);
        TypedQuery<Item> query = em.createQuery(jpql, Item.class);
        if (StringUtils.hasText(itemName)) {
            query.setParameter("itemName", itemName);
        }
        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }
        return query.getResultList();
    }
}
