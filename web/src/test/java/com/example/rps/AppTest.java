package com.example.rps;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Beka Tsotsoria
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class AppTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void requestingNonExistingGameShouldResultInNotFoundError() throws Exception {
        mvc.perform(get("/game/1"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    public void canCreateNewGame() throws Exception {
        String location = mvc.perform(post("/game"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrlPattern("/game/*"))
            .andDo(print())
            .andReturn()
            .getResponse().getHeader("Location");

        mvc.perform(get(location))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("gameId"));
    }
}
