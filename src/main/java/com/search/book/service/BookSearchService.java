package com.search.book.service;


import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.search.book.dto.BookSearchRequest;
import com.search.book.dto.BookSearchResponse;
import com.search.book.dto.KakaoBookResponse;
import com.search.book.dto.NaverBookResponse;
import com.search.book.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookSearchService {

    @Value("${api.kakao.url}")
    private String KAKAO_API_URL;
    @Value("${api.kakao.key}")
    private String KAKAO_API_KEY;

    @Value("${api.naver.url}")
    private String NAVER_API_URL;
    @Value("${api.naver.client-id}")
    private String NAVER_CLIENT_ID;
    @Value("${api.naver.client-secret}")
    private String NAVER_CLIENT_SECRET;

    // TODO : Feign으로 변환하기
    private final RestTemplate restTemplate;

    @HystrixCommand(commandKey="getKakaoBookSearchResult", fallbackMethod = "getNaverBookSearchResult")
    public ResponseEntity<BookSearchResponse> getKakaoBookSearchResult(UserPrincipal currentUser, BookSearchRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
        httpHeaders.add("Authorization", "KakaoAK " + KAKAO_API_KEY);

        KakaoBookResponse response = restTemplate.exchange(request.getKakaoUrl(KAKAO_API_URL + "/v3/search/book")
                                                            , HttpMethod.GET
                                                            , new HttpEntity<>(httpHeaders)
                                                            , KakaoBookResponse.class).getBody();

        // TODO : 카카오 API 응답값에 따른 예외 처리

        if (Objects.isNull(response)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(BookSearchResponse.kakaoBookResponseMapper(response, request));
    }

    public ResponseEntity<BookSearchResponse> getNaverBookSearchResult(UserPrincipal currentUser, BookSearchRequest request, Throwable t) {
        log.error("fallback method call !!! getKakaoBookSearchResult", t);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
        httpHeaders.add("X-Naver-Client-Id", NAVER_CLIENT_ID);
        httpHeaders.add("X-Naver-Client-Secret", NAVER_CLIENT_SECRET);

        NaverBookResponse response = restTemplate.exchange(request.getNaverUrl(NAVER_API_URL + "/v1/search/book.json")
                                                            , HttpMethod.GET
                                                            , new HttpEntity<>(httpHeaders)
                                                            , NaverBookResponse.class).getBody();

        // TODO : 네이버 API 응답값에 따른 예외 처리

        if (Objects.isNull(response)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(BookSearchResponse.naverResponseMapper(response));
    }

}
