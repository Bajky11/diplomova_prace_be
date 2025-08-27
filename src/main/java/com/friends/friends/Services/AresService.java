package com.friends.friends.Services;

import com.friends.friends.Exception.Account.AccountAlreadyExistsException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


public class AresService {

    RestTemplate restTemplate;

    public AresService() {
        this.restTemplate = new RestTemplate();
    }

    public boolean checkIfIcoIsValid(String ico) {
        try{
            String url = "https://ares.gov.cz/ekonomicke-subjekty-v-be/rest/ekonomicke-subjekty/" + ico;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        }catch(Exception e){
            System.out.println(e.getMessage());
            throw new AccountAlreadyExistsException("Submited ico is not valid");
        }
    }
}
