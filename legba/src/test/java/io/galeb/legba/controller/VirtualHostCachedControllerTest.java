package io.galeb.legba.controller;

import io.galeb.core.entity.VirtualHost;
import io.galeb.core.services.VersionService;
import io.galeb.legba.services.CopyService;
import io.galeb.legba.services.RoutersService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class VirtualHostCachedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VersionService versionService;

    @MockBean
    private CopyService copyService;

    @MockBean
    private RoutersService routersService;

    @Test
    public void shouldBadRequestWhenPassingApiVersionInexistent() throws Exception {
        when(versionService.getActualVersion("1")).thenReturn("2");
        when(versionService.getCache("1", "zone-local", "2")).thenReturn(null);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("If-None-Match", "1");
        httpHeaders.add("X-Galeb-GroupID", "group-local");
        httpHeaders.add("X-Galeb-ZoneID", "zone-local");
        this.mockMvc.perform(get("/xxx/virtualhostscached/1").headers(httpHeaders))
                .andDo(print())
                .andExpect(status().is(400));
    }

    @Test
    public void shouldOkResponseWhenNotPassingApiVersion() throws Exception {
        when(versionService.getActualVersion("1")).thenReturn("2");
        when(versionService.getCache("1", "zone-local","2")).thenReturn(null);
        when(routersService.get("1", "group-local")).thenReturn(1);

        List<VirtualHost> listVirtualHost = new ArrayList<>();
        when(copyService.getVirtualHosts("1")).thenReturn(listVirtualHost);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("If-None-Match", "1");
        httpHeaders.add("X-Galeb-GroupID", "group-local");
        this.mockMvc.perform(get("/virtualhostscached/1").headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"virtualhosts\":[]}"));
    }

    @Test
    public void shouldOkResponseWhenDoesNotExistsCache() throws Exception {
        when(versionService.getActualVersion("1")).thenReturn("2");
        when(versionService.getCache("1", "zone-local","2")).thenReturn(null);
        when(routersService.get("1", "group-local")).thenReturn(1);

        List<VirtualHost> listVirtualHost = new ArrayList<>();
        when(copyService.getVirtualHosts("1")).thenReturn(listVirtualHost);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("If-None-Match", "1");
        httpHeaders.add("X-Galeb-GroupID", "group-local");
        httpHeaders.add("X-Galeb-ZoneID", "zone-local");
        this.mockMvc.perform(get("/v1/virtualhostscached/1").headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"virtualhosts\":[]}"));
    }

    @Test
    public void shouldNotModifiedResponse() throws Exception {
        when(versionService.getActualVersion("1")).thenReturn("1");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("If-None-Match", "1");
        httpHeaders.add("X-Galeb-GroupID", "group-local");
        httpHeaders.add("X-Galeb-ZoneID", "zone-local");
        this.mockMvc.perform(get("/v1/virtualhostscached/1").headers(httpHeaders))
                .andDo(print())
                .andExpect(status().is(304));
    }

    @Test
    public void shouldNotFoundResponse() throws Exception {
        when(versionService.getActualVersion("1")).thenReturn("2");
        when(versionService.getCache("1", "zone-local","2")).thenReturn("");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("If-None-Match", "1");
        httpHeaders.add("X-Galeb-GroupID", "group-local");
        httpHeaders.add("X-Galeb-ZoneID", "zone-local");
        this.mockMvc.perform(get("/v1/virtualhostscached/1").headers(httpHeaders))
                .andDo(print())
                .andExpect(status().is(404));
    }

}
