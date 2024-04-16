package server.model;

import server.enumerated.HttpMethod;

import java.util.Optional;

public record RequestParams(HttpMethod httpMethod, Optional<Integer> requestParam, String requestBodyInString){

}


