package me.blockchain.stack;

import me.blockchain.components.Blockchain;
import me.blockchain.components.NodesRegistrar;
import me.blockchain.model.Block;
import me.blockchain.model.Transaction;
import me.blockchain.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Created by marcomele on 5/10/20
 */

@RestController
public class Controller {

    private static final String nodeID = UUID.randomUUID().toString().replaceAll("-", "");

    private final Blockchain blockchain;
    private final NodesRegistrar nodesRegistrar;

    @Autowired
    public Controller(final Blockchain blockchain, final NodesRegistrar nodesRegistrar) {
        this.blockchain = blockchain;
        this.nodesRegistrar = nodesRegistrar;
    }

    @GetMapping("mine")
    public ResponseEntity<?> mine() {
        final Block last = blockchain.lastBlock();
        final long proof = Blockchain.proofOfWork(last.getProof());
        blockchain.newTransaction("0", nodeID, 1);
        final String previousHash = Blockchain.hash(last);
        final Block next = blockchain.newBlock(proof, previousHash);
        return ResponseEntity.ok().body(next);
    }

    @PostMapping("transactions/new")
    public ResponseEntity<?> postTransaction(@RequestBody Transaction transaction) {
        final long index = blockchain.newTransaction(transaction.getSender(), transaction.getRecipient(),
                transaction.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(index);
    }

    @GetMapping("chain")
    public ResponseEntity<?> getChain() {
        return ResponseEntity.ok(JsonUtil.serialize(blockchain.getChain()));
    }

    @PostMapping("nodes/register/{address}")
    public ResponseEntity<?> registerNode(@PathVariable @NotNull final String address) {
        if (nodesRegistrar.registerNode(address)) {
            return ResponseEntity.accepted().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("nodes/resolve")
    public ResponseEntity<?> consensus() {
        final boolean replaced = nodesRegistrar.resolveConflicts();
        return ResponseEntity.ok(replaced);
    }

}
