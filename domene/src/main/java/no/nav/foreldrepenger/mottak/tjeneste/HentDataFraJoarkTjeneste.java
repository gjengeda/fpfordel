package no.nav.foreldrepenger.mottak.tjeneste;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.fordel.kodeverdi.VariantFormat;
import no.nav.foreldrepenger.mottak.journal.JournalDokument;
import no.nav.foreldrepenger.mottak.journal.JournalMetadata;
import no.nav.foreldrepenger.mottak.journal.JournalTjeneste;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class HentDataFraJoarkTjeneste {

    private JournalTjeneste journalTjeneste;

    @Inject
    public HentDataFraJoarkTjeneste(JournalTjeneste journalTjeneste) {
        this.journalTjeneste = journalTjeneste;

    }

    public HentDataFraJoarkTjeneste() {
    }

    public Optional<JournalMetadata<DokumentTypeId>> hentHoveddokumentMetadata(String journalpostId) {
        var hoveddokumenter = hentDokumentMetadata(journalpostId);
        if (!hoveddokumenter.isEmpty()) {
            if (erStrukturertDokument(hoveddokumenter)) {
                return Optional.of(hentMetadataForStrukturertDokument(hoveddokumenter));
            } else {
                return Optional.of(hentMetadataForUstrukturertDokument(hoveddokumenter));
            }
        }
        return Optional.empty();
    }

    public <T extends DokumentTypeId> Optional<JournalDokument<T>> hentStrukturertJournalDokument(
            JournalMetadata<T> journalMetadata) {
        if (ArkivFilType.XML.equals(journalMetadata.getArkivFilType())
                && erStrukturertDokumentVariantFormat(journalMetadata)) {
            return Optional.ofNullable(journalTjeneste.hentDokument(journalMetadata));
        }
        return Optional.empty();
    }

    public List<JournalMetadata<DokumentTypeId>> hentDokumentMetadata(String journalpostId) {
        return journalTjeneste.hentMetadata(journalpostId).stream()
                .filter(this::erHovedDokument)
                .collect(Collectors.toList());
    }

    public static <T extends DokumentTypeId> JournalMetadata<T> hentMetadataForStrukturertDokument(
            List<JournalMetadata<T>> hoveddokumenter) {
        return hoveddokumenter
                .stream()
                .filter(it -> ArkivFilType.XML.equals(it.getArkivFilType())).findFirst().get();
    }

    public static <T extends DokumentTypeId> JournalMetadata<T> hentMetadataForUstrukturertDokument(
            List<JournalMetadata<T>> hoveddokumenter) {
        return hoveddokumenter.stream()
                .filter(it -> VariantFormat.ARKIV.equals(it.getVariantFormat()))
                .findFirst()
                .get();
    }

    private boolean erHovedDokument(JournalMetadata<? extends DokumentTypeId> metadata) {
        return metadata.getErHoveddokument() && metadata.getArkivFilType() != null;
    }

    private static <T extends DokumentTypeId> boolean erStrukturertDokumentVariantFormat(JournalMetadata<T> it) {
        return VariantFormat.ORIGINAL.equals(it.getVariantFormat())
                || VariantFormat.FULLVERSJON.equals(it.getVariantFormat());
    }

    public static <T extends DokumentTypeId> boolean erStrukturertDokument(List<JournalMetadata<T>> hoveddokumenter) {
        return hoveddokumenter.stream()
                .filter(HentDataFraJoarkTjeneste::erStrukturertDokumentVariantFormat) // Ustrukturerte dokumenter kan ha
                                                                                      // xml med variantformat
                                                                                      // SKANNING_META
                .anyMatch(it -> ArkivFilType.XML.equals(it.getArkivFilType()));
    }

    /**
     * Henter ut dato for forsendelse mottatt. Finner vi ikke dato i metadata bruker
     * vi dagens dato.
     *
     * @param hovedDokumenter liste med hoveddokumenter
     * @return datoen forsendelsen er mottatt
     */
    public static LocalDate hentForsendelseMottatt(List<JournalMetadata<DokumentTypeId>> hovedDokumenter) {
        return hovedDokumenter.stream().filter(m -> m.getForsendelseMottatt() != null)
                .map(JournalMetadata::getForsendelseMottatt).findFirst().orElse(FPDateUtil.iDag());
    }

    public static LocalDateTime hentForsendelseMottattTidspunkt(List<JournalMetadata<DokumentTypeId>> hovedDokumenter) {
        return hovedDokumenter.stream().filter(m -> m.getForsendelseMottattTidspunkt() != null)
                .map(JournalMetadata::getForsendelseMottattTidspunkt).findFirst().orElse(FPDateUtil.nå());
    }

    public static DokumentTypeId hentDokumentTypeId(List<JournalMetadata<DokumentTypeId>> hovedDokumenter) {
        return hovedDokumenter.stream().filter(JournalMetadata::getErHoveddokument)
                .map(JournalMetadata::getDokumentTypeId).findFirst().orElse(DokumentTypeId.UDEFINERT);
    }

    public static Optional<DokumentKategori> hentDokumentKategori(List<JournalMetadata<DokumentTypeId>> hovedDokumenter) {
        return hovedDokumenter.stream().filter(JournalMetadata::getErHoveddokument)
                .map(JournalMetadata::getDokumentKategori).flatMap(Optional::stream).findFirst();
    }

    public static Optional<String> hentJournalførendeEnhet(List<JournalMetadata<DokumentTypeId>> hovedDokumenter) {
        return hovedDokumenter.stream().map(JournalMetadata::getJournalførendeEnhet).filter(Objects::nonNull)
                .findFirst();
    }

    public static BehandlingTema korrigerBehandlingTemaFraDokumentType(Tema tema, BehandlingTema behandlingTema,
            DokumentTypeId dokumentTypeId) {
        if (behandlingTema == null) {
            behandlingTema = BehandlingTema.UDEFINERT;
        }
        if (!Tema.OMS.equals(tema) && BehandlingTema.ikkeSpesifikkHendelse(behandlingTema)) {
            if (DokumentTypeId.erForeldrepengerRelatert(dokumentTypeId)) {
                behandlingTema = BehandlingTema.FORELDREPENGER;
                if (DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL.equals(dokumentTypeId)) {
                    behandlingTema = BehandlingTema.FORELDREPENGER_FØDSEL;
                } else if (DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON.equals(dokumentTypeId)) {
                    behandlingTema = BehandlingTema.FORELDREPENGER_ADOPSJON;
                }
            } else if (DokumentTypeId.erEngangsstønadRelatert(dokumentTypeId)) {
                behandlingTema = BehandlingTema.ENGANGSSTØNAD;
                if (DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL.equals(dokumentTypeId)) {
                    behandlingTema = BehandlingTema.ENGANGSSTØNAD_FØDSEL;
                } else if (DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON.equals(dokumentTypeId)) {
                    behandlingTema = BehandlingTema.ENGANGSSTØNAD_ADOPSJON;
                }
            } else if (DokumentTypeId.erSvangerskapspengerRelatert(dokumentTypeId)) {
                behandlingTema = BehandlingTema.SVANGERSKAPSPENGER;
            }
        }
        return behandlingTema;
    }

}