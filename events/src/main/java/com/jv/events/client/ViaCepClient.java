package com.jv.events.client;

import com.jv.events.dto.ViaCepResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "viacep-client", url = "https://viacep.com.br/ws")
public interface ViaCepClient {
    
    @GetMapping("/{cep}/json/")
    ViaCepResponse buscarCep(@PathVariable("cep") String cep);
}