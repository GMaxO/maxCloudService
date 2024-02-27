package ru.netology.mycloud.model;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
@Data
@Accessors(chain = true)
public class User {

    @Id
    @GeneratedValue
    private Long id;
    @NotNull
    private String login;
    @NotNull
    private String password;
}
