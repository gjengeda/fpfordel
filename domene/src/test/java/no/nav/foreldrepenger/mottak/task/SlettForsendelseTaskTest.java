package no.nav.foreldrepenger.mottak.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseTestUtil;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class SlettForsendelseTaskTest {
    private static final String SAKSNUMMER = "9876543";
    private static final String AKTØR_ID = "9000000000009";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private ProsessTaskRepository prosessTaskRepositoryMock;
    @Mock
    private DokumentRepository dokumentRepositoryMock;

    private SlettForsendelseTask task;
    private ProsessTaskData ptd;
    private UUID forsendelseId;

    @Before
    public void setup() {
        forsendelseId = UUID.randomUUID();
        prosessTaskRepositoryMock = mock(ProsessTaskRepository.class);
        dokumentRepositoryMock = mock(DokumentRepository.class);

        task = new SlettForsendelseTask(prosessTaskRepositoryMock, dokumentRepositoryMock);

        ptd = new ProsessTaskData(SlettForsendelseTask.TASKNAME);
        ptd.setSekvens("1");
    }

    @Test
    public void test_skalSletteJournalførtForsendelse() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);

        DokumentforsendelseTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        DokumentMetadata metadata = DokumentforsendelseTestUtil.lagMetadata(forsendelseId, SAKSNUMMER);
        metadata.setArkivId(SAKSNUMMER);
        metadata.setStatus(ForsendelseStatus.GOSYS);

        when(dokumentRepositoryMock.hentUnikDokumentMetadata(any(UUID.class))).thenReturn(Optional.of(metadata));

        data.setForsendelseId(forsendelseId);
        data.setAktørId(AKTØR_ID);
        data.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper target = task.doTask(data);

        verify(dokumentRepositoryMock).slettForsendelse(forsendelseId);
        assertThat(target).isNull();
    }

    @Test
    public void test_skalIkkeSlettePendingForsendelse() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);

        DokumentforsendelseTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        DokumentMetadata metadata = DokumentforsendelseTestUtil.lagMetadata(forsendelseId, SAKSNUMMER);
        metadata.setStatus(ForsendelseStatus.PENDING);

        when(dokumentRepositoryMock.hentUnikDokumentMetadata(any(UUID.class))).thenReturn(Optional.of(metadata));

        data.setForsendelseId(forsendelseId);
        data.setAktørId(AKTØR_ID);
        data.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        task.doTask(data);

        verify(dokumentRepositoryMock, never()).slettForsendelse(any());
    }

    @Test
    public void test_skalIkkeSletteForsendelseUtenSaksnummer() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);

        DokumentforsendelseTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        DokumentMetadata metadata = DokumentforsendelseTestUtil.lagMetadata(forsendelseId, null);
        metadata.setStatus(ForsendelseStatus.FPSAK);

        when(dokumentRepositoryMock.hentUnikDokumentMetadata(any(UUID.class))).thenReturn(Optional.of(metadata));

        data.setForsendelseId(forsendelseId);
        data.setAktørId(AKTØR_ID);
        data.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        task.doTask(data);

        verify(dokumentRepositoryMock, never()).slettForsendelse(any());
    }
}
