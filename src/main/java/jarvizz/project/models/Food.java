package jarvizz.project.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@EqualsAndHashCode
@ToString(exclude = {"user","orders"})
@Getter
@Setter
public class Food implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Nationalized
    private String name;
    @Enumerated(EnumType.STRING)
    private Type type;
    private float weight;
    private float price;
    @Nationalized
    private String description;
    private String picture;
    @JsonIgnore
    @ManyToMany(cascade = CascadeType.ALL,fetch = FetchType.LAZY )
    private List<User> user = new ArrayList<>();
    @JsonIgnore
    @ManyToMany(cascade =  {CascadeType.DETACH,CascadeType.PERSIST,CascadeType.DETACH,CascadeType.REMOVE,CascadeType.REFRESH,},fetch = FetchType.LAZY)
    private List<Orders> orders = new ArrayList<>();

    public Food(String name, Type type, float weight, float price, String description, String picture, List<User> user) {
        this.name = name;
        this.type = type;
        this.weight = weight;
        this.price = price;
        this.description = description;
        this.picture = picture;
        this.user = user;
    }

    public Food( int id, String name, Type type, float weight, float price, String description, String picture) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.weight = weight;
        this.price = price;
        this.description = description;
        this.picture = picture;
    }

}

