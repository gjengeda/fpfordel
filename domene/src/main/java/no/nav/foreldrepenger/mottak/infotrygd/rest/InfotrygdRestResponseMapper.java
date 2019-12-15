package no.nav.foreldrepenger.mottak.infotrygd.rest;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static no.nav.foreldrepenger.fordel.kodeverdi.RelatertYtelseTema.FORELDREPENGER_TEMA;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import no.nav.foreldrepenger.fordel.kodeverdi.RelatertYtelseBehandlingstema;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdSak;

public final class InfotrygdRestResponseMapper {

    private static final String FA = FORELDREPENGER_TEMA.getKode();

    private final String behandlingstema;

    public InfotrygdRestResponseMapper(RelatertYtelseBehandlingstema behandlingstema) {
        this.behandlingstema = behandlingstema.getKode();
    }

    public String getBehandlingstema() {
        return behandlingstema;
    }

    public List<InfotrygdSak> map(Saker saker) {
        return infotrygdSakerFra(
                infotrygdSakerFraIkkeStartedeSaker(saker.getIkkeStartedeSaker()),
                infotrygdSakerFraLøpendeSaker(saker.getLøpendeSaker()),
                infotrygdSakerFraAvsluttedeSaker(saker.getAvsluttedeSaker()),
                infotrygdSakerFraSaker(saker.getSaker()));
    }

    private Stream<InfotrygdSak> infotrygdSakerFraIkkeStartedeSaker(List<IkkeStartetSak> ikkeStartedeSaker) {
        return stream(ikkeStartedeSaker)
                .map(this::tilInfotrygdSak);
    }

    private Stream<InfotrygdSak> infotrygdSakerFraSaker(List<Sak> saker) {
        return stream(saker)
                .map(this::tilInfotrygdSak);
    }

    private Stream<InfotrygdSak> infotrygdSakerFraAvsluttedeSaker(AvsluttedeSaker avsluttedeSaker) {
        return stream(avsluttedeSaker.getSaker())
                .map(this::tilInfotrygdSak);
    }

    private Stream<InfotrygdSak> infotrygdSakerFraLøpendeSaker(List<LøpendeSak> løpendeSaker) {
        return stream(løpendeSaker)
                .map(this::tilInfotrygdSak);
    }

    private InfotrygdSak tilInfotrygdSak(AvsluttetSak sak) {
        return new InfotrygdSak(null, FA, behandlingstema, sak.getIverksatt(), null);
    }

    private InfotrygdSak tilInfotrygdSak(IkkeStartetSak sak) {
        return new InfotrygdSak(null, FA, behandlingstema, sak.getIverksatt(), sak.getRegistrert());
    }

    private InfotrygdSak tilInfotrygdSak(Sak sak) {
        return new InfotrygdSak(sak.getSaksnummer(), FA, behandlingstema, sak.getIverksatt(), sak.getVedtatt());
    }

    private InfotrygdSak tilInfotrygdSak(LøpendeSak sak) {
        return new InfotrygdSak(null, FA, behandlingstema, sak.getIverksatt(), null);
    }

    private static <T> Stream<T> stream(List<T> list) {
        return Optional.ofNullable(list)
                .orElseGet(Collections::emptyList)
                .stream();
    }

    private static <T extends InfotrygdSak> List<T> infotrygdSakerFra(Stream<T> s1, Stream<T> s2, Stream<T> s3,
            Stream<T> s4) {
        return concat(s1, concat(s2, concat(s3, s4)))
                .collect(toList());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[behandlingstema=" + behandlingstema + "]";
    }
}
