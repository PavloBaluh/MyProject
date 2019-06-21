package jarvizz.project.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;

@Entity
@Data
@ToString(exclude = {"user"})
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Nationalized
    private String name;
    @Nationalized
    private String surname;
    private String phoneNumber;
    @Nationalized
    private String address;
    private double bonus = 0.0;
    private String picture;
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private User user;
    public UserInfo(String name, String surname, String phoneNumber, String address) {
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }
    public UserInfo(String name, String surname, String phoneNumber, String address, double bonus) {
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.bonus = bonus;
    }
}
