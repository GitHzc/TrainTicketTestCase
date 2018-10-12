package org.services.test.controller;

import org.services.test.entity.VoucherRequestDto;
import org.services.test.entity.VoucherResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class VoucherController {

    @Bean
    public RestTemplate restTemplate(){
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        //Add the Jackson Message converter
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        // Note: here we are making this converter to process any kind of response,
        // not only application/*json, which is the default behaviour
        converter.setSupportedMediaTypes(Arrays.asList(MediaType.ALL));
        messageConverters.add(converter);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(messageConverters);

        return restTemplate;
    }

    @Autowired
    RestTemplate restTemplate;
    @GetMapping("/test")
    public VoucherResponseDto getVoucherServiceTestCase() {

        VoucherRequestDto vrd = new VoucherRequestDto("d4a8c3ed-b015-4324-a62f-2592a5450b4a", 1);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<VoucherRequestDto> req = new HttpEntity<>(vrd, headers);
        ResponseEntity<VoucherResponseDto> re = restTemplate.exchange("http://10.141.212.146:31380/getVoucher", HttpMethod.POST, req, VoucherResponseDto.class);
        VoucherResponseDto resp = re.getBody();
        if (null != resp) {
            return resp;
        } else {
            throw new RuntimeException("sql error");
        }
    }


}
