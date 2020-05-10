package me.blockchain.model;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Created by marcomele on 5/10/20
 */

@Data
public class Block {

    @Getter
    @Accessors(fluent = true)
    private static final Block genesis = new Block();
    static {
        genesis.setIndex(1);
        genesis.setPreviousHash("1");
        genesis.setProof(100L);
    }

    private long index;
    private long timestamp;
    private List<Transaction> transactions;
    private long proof;
    private String previousHash;

}
