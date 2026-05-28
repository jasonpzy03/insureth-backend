package com.insureth.insurance.service.flightinsurance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
public class BlockchainConfig {

    @Bean(destroyMethod = "shutdown")
    @Primary
    public Web3j web3j(@Value("${blockchain.rpc-url:http://127.0.0.1:8545}") String rpcUrl) {
        return Web3j.build(new HttpService(rpcUrl));
    }

    @Bean(name = "governanceWeb3j", destroyMethod = "shutdown")
    public Web3j governanceWeb3j(
            @Value("${insurance-governance.rpc-url:${blockchain.rpc-url:http://127.0.0.1:8545}}") String rpcUrl
    ) {
        return Web3j.build(new HttpService(rpcUrl));
    }
}
