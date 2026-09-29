package com.codewithpaolo.frischcheck.service.notification;

import com.codewithpaolo.frischcheck.exception.NotificationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    @Test
    void notifyAll_failingChannelDoesNotBlockOtherChannels() {
        NotificationSender email = mock(NotificationSender.class);
        NotificationSender telegram = mock(NotificationSender.class);
        when(email.channelName()).thenReturn("E-Mail");
        doThrow(new NotificationException("SMTP down")).when(email).send("Betreff", "Text");

        int successful = new NotificationService(List.of(email, telegram)).notifyAll("Betreff", "Text");

        assertThat(successful).isEqualTo(1);
        verify(telegram).send("Betreff", "Text");
    }

    @Test
    void notifyAll_withoutChannels_returnsZero() {
        assertThat(new NotificationService(List.of()).notifyAll("Betreff", "Text")).isZero();
    }
}
