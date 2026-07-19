package com.petadoption.common.events;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdoptionEventsTest {
  @Test
  void exposesAdoptionEventNames() {
    assertThat(AdoptionEvents.SUBMITTED).isEqualTo("adoption.submitted");
    assertThat(AdoptionEvents.APPROVED).isEqualTo("adoption.approved");
    assertThat(AdoptionEvents.REJECTED).isEqualTo("adoption.rejected");
    assertThat(AdoptionEvents.CANCELLED).isEqualTo("adoption.cancelled");
  }
}
