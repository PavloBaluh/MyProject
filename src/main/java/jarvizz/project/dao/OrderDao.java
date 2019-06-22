package jarvizz.project.dao;

import jarvizz.project.models.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestHeader;

@Repository
public interface OrderDao extends JpaRepository<Orders,Integer> {

//    @Query("delete   from  orders   where  id=:integer")
    void deleteOrdersById(Integer integer);
}
