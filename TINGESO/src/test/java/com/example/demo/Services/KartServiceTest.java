package com.example.demo.Services;

import com.example.demo.Entities.Kart;
import com.example.demo.Repositories.KartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class KartServiceTest {

    @Mock
    private KartRepository kartRepository;

    @InjectMocks
    private KartService kartService;

    private Kart kart;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        kart = new Kart(1L, "Modelo A", "ABC123");
    }

    @Test
    void testFindAll() {
        // Arrange
        when(kartRepository.findAll()).thenReturn(Arrays.asList(kart));

        // Act
        var result = kartService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(kart, result.get(0));
    }

    @Test
    void testFindById_Found() {
        // Arrange
        when(kartRepository.findById(1L)).thenReturn(Optional.of(kart));

        // Act
        Optional<Kart> result = kartService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(kart, result.get());
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(kartRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<Kart> result = kartService.findById(1L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testSave() {
        // Arrange
        when(kartRepository.save(kart)).thenReturn(kart);

        // Act
        Kart result = kartService.save(kart);

        // Assert
        assertNotNull(result);
        assertEquals(kart, result);
        verify(kartRepository, times(1)).save(kart);
    }

    @Test
    void testDeleteById() {
        // Act
        kartService.deleteById(1L);

        // Assert
        verify(kartRepository, times(1)).deleteById(1L);
    }

    @Test
    void testUpdate_Found() {
        // Arrange
        Kart updatedKart = new Kart(1L, "Modelo B", "DEF456");
        when(kartRepository.findById(1L)).thenReturn(Optional.of(kart));
        when(kartRepository.save(any(Kart.class))).thenReturn(updatedKart);

        // Act
        Kart result = kartService.update(1L, updatedKart);

        // Assert
        assertNotNull(result);
        assertEquals("Modelo B", result.getModelo());
        assertEquals("DEF456", result.getCodificacion());
    }

    @Test
    void testUpdate_NotFound() {
        // Arrange
        Kart updatedKart = new Kart(1L, "Modelo B", "DEF456");
        when(kartRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Kart result = kartService.update(1L, updatedKart);

        // Assert
        assertNull(result);
    }
}

