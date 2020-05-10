package me.blockchain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by marcomele on 5/10/20
 */

@Data
@AllArgsConstructor
public class Transaction {

    private String sender;
    private String recipient;
    private double amount;

}
