package com.hospi.manage.features.room.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.common.service.ImageCompressionService;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.entity.RoomTypePicture;
import com.hospi.manage.features.room.repository.RoomTypePictureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomTypePictureServiceTest {

    @Mock
    private ImageCompressionService imageCompressionService;

    @Mock
    private RoomTypePictureRepository roomTypePictureRepository;

    @InjectMocks
    private RoomTypePictureService service;

    private RoomType roomType;

    @BeforeEach
    void setUp() {
        roomType = new RoomType();
        roomType.setPictures(new ArrayList<>());
    }

    @Test
    void shouldReturn_whenFound() {
        RoomTypePicture picture = new RoomTypePicture();
        picture.setId(1L);

        when(roomTypePictureRepository.findById(1L))
                .thenReturn(Optional.of(picture));

        RoomTypePicture result = service.findById(1L);

        assertEquals(1L, result.getId());
        verify(roomTypePictureRepository).findById(1L);
    }

    @Test
    void replaceCover_shouldReplaceExistingCover() throws Exception {
        RoomTypePicture oldCover = new RoomTypePicture();
        oldCover.setId(1L);
        oldCover.setSortOrder(1);

        roomType.getPictures().add(oldCover);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "data".getBytes()
        );

        when(imageCompressionService.toWebp(any(),
                anyInt(),
                anyFloat()))
                .thenReturn("compressed".getBytes());

        service.replaceCover(roomType, file);

        assertEquals(1, roomType.getPictures().size());

        RoomTypePicture newCover = roomType.getPictures().get(0);
        assertEquals(1, newCover.getSortOrder());
        assertArrayEquals("compressed".getBytes(), newCover.getImageData());

        verify(imageCompressionService).toWebp(any(),
                anyInt(),
                anyFloat());
    }

    @Test
    void replaceCover_shouldDoNothing_whenFileEmpty() throws Exception {
        service.replaceCover(roomType, null);
        assertTrue(roomType.getPictures().isEmpty());

        service.replaceCover(roomType,
                new MockMultipartFile("file",
                        new byte[0]));

        assertTrue(roomType.getPictures().isEmpty());
        verifyNoInteractions(imageCompressionService);
    }

    @Test
    void shouldAdd_whenFilesProvided() throws Exception {
        List<MultipartFile> files = List.of(
                new MockMultipartFile("f1",
                        "a.jpg",
                        "image/jpeg",
                        "a".getBytes()),
                new MockMultipartFile("f2",
                        "b.jpg",
                        "image/jpeg",
                        "b".getBytes())
        );

        when(imageCompressionService.toWebp(any(),
                anyInt(),
                anyFloat()))
                .thenReturn("compressed".getBytes());

        service.addNewImages(roomType, files);

        assertEquals(2, roomType.getPictures().size());
        assertTrue(roomType.getPictures().stream()
                .allMatch(p -> p.getSortOrder() == 2));

        verify(imageCompressionService, times(2))
                .toWebp(any(), anyInt(), anyFloat());
    }

    @Test
    void shouldRemoveById_whenIdsProvided() {
        RoomTypePicture p1 = new RoomTypePicture();
        p1.setId(1L);

        RoomTypePicture p2 = new RoomTypePicture();
        p2.setId(2L);

        roomType.getPictures().addAll(List.of(p1, p2));

        service.removeImages(roomType, List.of(1L));

        assertEquals(1, roomType.getPictures().size());
        assertEquals(2L, roomType.getPictures().get(0).getId());
    }

    @Test
    void shouldThrow_whenNotFound() {
        when(roomTypePictureRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.findById(99L));
    }
}
