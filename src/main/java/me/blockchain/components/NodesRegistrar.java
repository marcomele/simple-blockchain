package me.blockchain.components;

import com.google.common.collect.Iterators;
import me.blockchain.model.Block;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by marcomele on 5/10/20
 */

@Service
public class NodesRegistrar {

    private final Set<String> nodes = new HashSet<>();
    private final Blockchain blockchain;
    private final RestTemplate restTemplate;

    @Autowired
    public NodesRegistrar(final Blockchain blockchain, final RestTemplate restTemplate) {
        this.blockchain = blockchain;
        this.restTemplate = restTemplate;
    }

    public boolean registerNode(@NotNull final String address) {
        return nodes.add(address);
    }

    public static boolean isBlockchainValid(final List<Block> chain) {
        Block last = Iterators.getLast(chain.iterator());
        for (Block block : chain) {
            if (!block.getPreviousHash().equals(Blockchain.hash(last))) {
                return false;
            }
            if (!Blockchain.validateProof(last.getProof(), block.getProof())) {
                return false;
            }
            last = block;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean resolveConflicts() {
        List<Block> longestChain = null;
        long maxLength = blockchain.getChain().size();
        for (String node : nodes) {
            final URI uri = UriComponentsBuilder.fromUriString("http://{node}/chain").buildAndExpand(node).toUri();
            final ResponseEntity<?> response = restTemplate.getForEntity(uri, List.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                final List<Block> chain = (List<Block>) response.getBody();
                // noinspection ConstantConditions
                if (CollectionUtils.size(chain) > maxLength && isBlockchainValid(chain)) {
                    longestChain = chain;
                    maxLength = chain.size();
                }
            }
        }
        if (null != longestChain) {
            blockchain.replaceBlockchain(longestChain);
            return true;
        }
        return false;
    }

}
