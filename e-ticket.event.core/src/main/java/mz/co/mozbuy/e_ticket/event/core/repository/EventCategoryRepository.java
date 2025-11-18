package mz.co.mozbuy.e_ticket.event.core.repository;

import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.model.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventCategoryRepository extends JpaRepository<EventCategory, Long>, JpaSpecificationExecutor<EventCategory> {


    /**
     * Encontra categoria por nome (case insensitive)
     */
    Optional<EventCategory> findByNameIgnoreCase(String name);

    /**
     * Encontra categoria por nome exato
     */
    Optional<EventCategory> findByName(String name);

    /**
     * Lista todas as categorias ativas
     */
    List<EventCategory> findByLifeCycleState(LifeCycleState lifeCycleState);

    /**
     * Verifica se existe categoria com o mesmo nome (ignorando case)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Verifica se existe categoria com o mesmo nome e ID diferente (para updates)
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    /**
     * Busca categorias por parte do nome
     */
    List<EventCategory> findByNameContainingIgnoreCase(String name);

    /**
     * Busca categorias com contagem de eventos
     */
    @Query("SELECT ec, COUNT(e) as eventCount FROM EventCategory ec LEFT JOIN ec.events e WHERE ec.lifeCycleState = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE GROUP BY ec")
    List<Object[]> findAllWithEventCount();

    /**
     * Busca categorias populares (com mais eventos)
     */
    @Query("SELECT ec FROM EventCategory ec WHERE ec.lifeCycleState =  mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE AND SIZE(ec.events) > 0 ORDER BY SIZE(ec.events) DESC")
    List<EventCategory> findPopularCategories();

    /**
     * Conta quantos eventos tem uma categoria
     */
    @Query("SELECT COUNT(e) FROM EventCategory ec JOIN ec.events e WHERE ec.id = :categoryId")
    Long countEventsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Busca categorias por código de cor
     */
    List<EventCategory> findByColorCode(String colorCode);

    /**
     * Busca categorias que possuem ícone
     */
    List<EventCategory> findByIconUrlIsNotNull();

    /**
     * Busca categorias com estatísticas completas
     */
    @Query("SELECT ec, COUNT(e) as eventCount, " +
            "SUM(CASE WHEN e.lifeCycleState = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE THEN 1 ELSE 0 END) as activeEvents " +
            "FROM EventCategory ec LEFT JOIN ec.events e " +
            "WHERE ec.lifeCycleState = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE " +
            "GROUP BY ec " +
            "ORDER BY eventCount DESC")
    List<Object[]> findCategoriesWithStatistics();
}