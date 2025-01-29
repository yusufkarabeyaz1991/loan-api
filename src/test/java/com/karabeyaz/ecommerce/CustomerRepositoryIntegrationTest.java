package com.karabeyaz.ecommerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.karabeyaz.application.LoanApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { LoanApplication.class })
@WebAppConfiguration
public class CustomerRepositoryIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mvc;

    @Before
    public void setup() throws Exception {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }


    @Test
    public void addAndListProducts() throws Exception {

        String requestBodyJson = "{ \"title\":\"val1\", \"price\": 20, \"category\":\"val2\" }";

        MvcResult addResult = mvc
                .perform(post("/products").content(requestBodyJson).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        MvcResult listResult = mvc.perform(get("/products")).
                andExpect(status().isOk()).andReturn();

        MockHttpServletResponse addResultResponse = addResult.getResponse();
        String addResultResponseContentAsString = addResultResponse.getContentAsString();

        MockHttpServletResponse listResultResponse = listResult.getResponse();
        String listResultResponseContentAsString = listResultResponse.getContentAsString();

        System.out.println(addResultResponseContentAsString);
        System.out.println(listResultResponseContentAsString);
        assertThat(listResultResponseContentAsString);
    }
}