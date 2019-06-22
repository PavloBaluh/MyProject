package jarvizz.project.sevices;

import jarvizz.project.dao.OrderDao;
import jarvizz.project.models.Orders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrderService {
    @Autowired
    OrderDao orderDao;

    public void save(Orders orders){
        orderDao.save(orders);
    }
    public List<Orders> getAllOrders(){
        return this.orderDao.findAll();
    }
    public Orders findById(int id){
        return orderDao.getOne(id);
    }
    public  void  delete(Orders orders){
        System.out.println(orders.getId());
        orderDao.deleteOrdersById(orders.getId());
    }
}
