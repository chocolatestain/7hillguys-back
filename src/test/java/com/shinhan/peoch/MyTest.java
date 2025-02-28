package com.shinhan.peoch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class MyTest {

    @Mock
    private MyService myService;

    @InjectMocks
    private MyController myController;

    @Test
    void testMockito() {
        when(myService.getData()).thenReturn("Mocked Data");

        String result = myController.getData();

        assertEquals("Mocked Data", result);
        verify(myService, times(1)).getData();
    }
}