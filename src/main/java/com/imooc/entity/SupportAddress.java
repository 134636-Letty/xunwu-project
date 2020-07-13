package com.imooc.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "support_address")
public class SupportAddress {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "belong_to")
    private String belongTo;

    @Column(name = "en_name")
    private String enName;

    @Column(name = "cn_name")
    private String cnName;

    @Column(name = "level")
    private String level;

    public enum Level{
        CITY("city"),
        REGION("region");

        private String value;

        Level(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        private static Level of(String value){
           for (Level level : Level.values()){
               if (level.equals(value)){
                   return level;
               }
           }
           throw new IllegalArgumentException();
       }
    }







}
