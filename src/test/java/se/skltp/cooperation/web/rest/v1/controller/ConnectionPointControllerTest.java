package se.skltp.cooperation.web.rest.v1.controller;

import org.dozer.DozerBeanMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.skltp.cooperation.Application;
import se.skltp.cooperation.domain.ConnectionPoint;
import se.skltp.cooperation.service.ConnectionPointService;
import se.skltp.cooperation.web.rest.exception.ResourceNotFoundException;
import se.skltp.cooperation.web.rest.v1.dto.ConnectionPointDTO;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

/**
 * Test class for the ConnectionPointController REST controller.
 *
 * @author Peter Merikan
 * @see ConnectionPointController
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class ConnectionPointControllerTest {

	@InjectMocks
	ConnectionPointController uut;
	@Mock
	private ConnectionPointService connectionPointServiceMock;
	@Mock
	private DozerBeanMapper mapperMock;
	private MockMvc mockMvc;

	@PostConstruct
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(uut).build();
	}


	@Test
	public void getAllAsJson_shouldReturnAll() throws Exception {

		ConnectionPoint cp1 = new ConnectionPoint();
		ConnectionPoint cp2 = new ConnectionPoint();
		ConnectionPointDTO dto1 = new ConnectionPointDTO();
		dto1.setId(1L);
		dto1.setPlatform("dt01.platform");
		dto1.setEnvironment("dto1.environment");
		ConnectionPointDTO dto2 = new ConnectionPointDTO();
		dto2.setId(2L);
		dto2.setPlatform("dt02.platform");
		dto2.setEnvironment("dto2.environment");

		when(connectionPointServiceMock.findAll()).thenReturn(Arrays.asList(cp1, cp2));
		when(mapperMock.map(cp1, ConnectionPointDTO.class)).thenReturn(dto1);
		when(mapperMock.map(cp2, ConnectionPointDTO.class)).thenReturn(dto2);

		// Get all the connectionPoints
		mockMvc.perform(get("/v1/connectionPoints").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$.[0].id").value(is(dto1.getId().intValue())))
			.andExpect(jsonPath("$.[0].platform").value(is(dto1.getPlatform())))
			.andExpect(jsonPath("$.[0].environment").value(is(dto1.getEnvironment())))
			.andExpect(jsonPath("$.[1].id").value(is(dto2.getId().intValue())))
			.andExpect(jsonPath("$.[1].platform").value(is(dto2.getPlatform())))
			.andExpect(jsonPath("$.[1].environment").value(is(dto2.getEnvironment())));

		verify(connectionPointServiceMock, times(1)).findAll();
		verifyNoMoreInteractions(connectionPointServiceMock);

	}

	@Test
	public void getAllAsXml_shouldReturnAll() throws Exception {

		ConnectionPoint cp1 = new ConnectionPoint();
		ConnectionPoint cp2 = new ConnectionPoint();
		ConnectionPointDTO dto1 = new ConnectionPointDTO();
		dto1.setId(1L);
		dto1.setPlatform("dt01.platform");
		dto1.setEnvironment("dto1.environment");
		ConnectionPointDTO dto2 = new ConnectionPointDTO();
		dto2.setId(2L);
		dto2.setPlatform("dt02.platform");
		dto2.setEnvironment("dto2.environment");

		when(connectionPointServiceMock.findAll()).thenReturn(Arrays.asList(cp1, cp2));
		when(mapperMock.map(cp1, ConnectionPointDTO.class)).thenReturn(dto1);
		when(mapperMock.map(cp2, ConnectionPointDTO.class)).thenReturn(dto2);

		// Get all the connectionPoints
		mockMvc.perform(get("/v1/connectionPoints").accept(MediaType.APPLICATION_XML))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_XML))
			.andExpect(xpath("/connectionPoints/connectionPoint[1]/id").string(is(dto1.getId().toString())))
			.andExpect(xpath("/connectionPoints/connectionPoint[1]/platform").string(is(dto1.getPlatform())))
			.andExpect(xpath("/connectionPoints/connectionPoint[1]/environment").string(is(dto1.getEnvironment())))
			.andExpect(xpath("/connectionPoints/connectionPoint[2]/id").string(is(dto2.getId().toString())))
			.andExpect(xpath("/connectionPoints/connectionPoint[2]/platform").string(is(dto2.getPlatform())))
			.andExpect(xpath("/connectionPoints/connectionPoint[2]/environment").string(is(dto2.getEnvironment())));

		verify(connectionPointServiceMock, times(1)).findAll();
		verifyNoMoreInteractions(connectionPointServiceMock);

	}

	@Test
	public void getAllAsJson_shouldReturnEmptyList() throws Exception {

		when(connectionPointServiceMock.findAll()).thenReturn(Collections.emptyList());

		mockMvc.perform(get("/v1/connectionPoints").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	public void getAllAsXml_shouldReturnEmptyList() throws Exception {

		when(connectionPointServiceMock.findAll()).thenReturn(Collections.emptyList());

		mockMvc.perform(get("/v1/connectionPoints").accept(MediaType.APPLICATION_XML))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_XML))
			.andExpect(xpath("/connectionPoints").nodeCount(1))
			.andExpect(xpath("/connectionPoints/*").nodeCount(0));

	}

	@Test
	public void get_shouldReturnOneAsJson() throws Exception {
		ConnectionPoint cp1 = new ConnectionPoint();
		cp1.setId(1L);
		ConnectionPointDTO dto1 = new ConnectionPointDTO();
		dto1.setId(1L);
		dto1.setPlatform("dt01.platform");
		dto1.setEnvironment("dto1.environment");

		when(connectionPointServiceMock.find(cp1.getId())).thenReturn(cp1);
		when(mapperMock.map(cp1, ConnectionPointDTO.class)).thenReturn(dto1);

		// Get the cp1
		mockMvc.perform(get("/v1/connectionPoints/{id}", cp1.getId())
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.id").value(dto1.getId().intValue()))
			.andExpect(jsonPath("$.platform").value(dto1.getPlatform()))
			.andExpect(jsonPath("$.environment").value(dto1.getEnvironment()));
	}

	@Test
	public void get_shouldReturnOneAsXml() throws Exception {
		ConnectionPoint cp1 = new ConnectionPoint();
		cp1.setId(1L);
		ConnectionPointDTO dto1 = new ConnectionPointDTO();
		dto1.setId(1L);
		dto1.setPlatform("dt01.platform");
		dto1.setEnvironment("dto1.environment");

		when(connectionPointServiceMock.find(cp1.getId())).thenReturn(cp1);
		when(mapperMock.map(cp1, ConnectionPointDTO.class)).thenReturn(dto1);

		// Get the cp1
		mockMvc.perform(get("/v1/connectionPoints/{id}", cp1.getId())
			.accept(MediaType.APPLICATION_XML))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_XML))
			.andExpect(xpath("/connectionPoint/id").string(is(dto1.getId().toString())))
			.andExpect(xpath("/connectionPoint/platform").string(is(dto1.getPlatform())))
			.andExpect(xpath("/connectionPoint/environment").string(is(dto1.getEnvironment())));
	}

	@Test
	public void get_shouldThrowNotFoundException() throws Exception {

		when(connectionPointServiceMock.find(anyLong())).thenReturn(null);
		try {
			mockMvc.perform(get("/v1/connectionPoints/{id}", Long.MAX_VALUE))
				.andExpect(status().isNotFound());
			fail("Should thrown a exception");
		} catch (Exception e) {
			assertEquals(e.getCause().getClass(), ResourceNotFoundException.class);
		}
	}

}
