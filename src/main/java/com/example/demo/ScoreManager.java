package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ScoreManager {

    private ConcurrentHashMap<String, Integer> scores;
    private ConcurrentHashMap<String, String> names;
    private final static String[] PREFIXES = ("덜렁대는,냉정한,차분한,얌전한,신중한,온순한,건방진,겁쟁이,성급한,명랑한,천진난만한,성실한,까칠한," +
            "배고픈,변덕쟁이,외로운,고집쟁이,개구쟁이,용감한,대담한,수줍은,촐랑대는,조심스러운,의젓한,노력하는,활동적인,포용력이 있는,격하기 쉬운,코믹한," +
            "적극적인,자기만족의,맥 빠진,우스꽝스러운,침략적인,자신 있는,무능력한,오만한,호전적인,확신 있는,아름다운,독립심이 강한,명확한,신중한,깨끗한," +
            "내성적인,예술적인,건설적인,감각이 있는,직관적인,우아한,협동적인,깨끗한,성미가 급한,단정한,대담한,청순한,안달하는,독단적인,용기 있는,다감한," +
            "위태로운,매력이 있는,성난,사랑스러운,미숙한,의식이 있는,화 잘내는,다루기 쉬운,불손한,무 감정의,창조적인,융통성이 있는,질투 많은,냉담한,독창적인," +
            "부서지기 쉬운,샘내는,거만한,호기심이 강한,몸이 약한,경계심이 강한,주의 깊은,경쟁적인,검소한,정정당당한,사려 깊은,귀여운,깔끔한,유식한,괴팍한," +
            "예속적인,의지가 약한,총명한,쾌활한,의지하고 있는,저능의,충실한,유망한,믿을 수 있는,우호적인,성실한,지루한,지시적인,친절한,우울한,따분한,단호한," +
            "붙임성 있는,호색적인,호전적인,독단적인,잘 지껄이는,능란한,까다로운,무딘,관대한,교활한,뻔뻔스러운,아둔한,편견 없는,성숙한,유능한,지루한,온화한," +
            "겸손한,능력 있는,활기가 없는,얌전한,소심한,주의 깊은,선명하지,않는,품위 있는,부정적인,매력적인,침울한,온후한,순진한,아름다운,엄한,인자한," +
            "쉽게 속는기운찬,태평스러운,용감한,신경질적인,명랑한,나태한,기운찬,흥분하기 쉬운,어린애 같은,유능한,멍청한,고집 센,유치한,능률적인,속기 쉬운," +
            "무방비적인,인정이 많은,이기적인,호인 같은,낙천적인,동적인,정에 약한,희망에 차 있는,집착하는,정적인,원기 왕성한,전도 유망한,강박 관념을 가진," +
            "유능한,시기하는,정직한,거만한,고집 센멍청한,유치한능률적인속기 쉬운,무방비적인,인정이 많은,이기적인,호인 같은,낙천적인,동적인,정에 약한,망" +
            "희에 차있는,집착하는,정적인,원기 왕성한,전도 유망한,강박 관념을 가진,근심스러운열광적인성실한당당한보수적인,외향적인,유머러스한,열정적인,내성적인," +
            "익살스러운,성급한,냉담한,조심성 많은,친밀한,둔감한,어리석은,열심인,옹졸한,단순한,힘 없는,노련한,바보 같은,우유부단한,분별력이 있는,자존심 센," +
            "박력 없는,편견을 가진,악의 있는,재치 있는,적극적인,성마른,익살스러운,명확한,못 마땅한,감상적인,성가신,진지한,감정적인,정치적인,수상쩍은," +
            "현실적인,긍정적인,숨기는,솔직담백한,잘난체하는,솔직하지 않는,인간적인,직관력 있는,굳센,격을 따지지 않는,조용한,힘찬,내성적인,얌전한,강한," +
            "수수한,기운찬순진한,신뢰할 수 있는,건강한,단순한,확실한,학문적인,어리석은,경건한,가무에 능한,바보같은,종교적인,거드름 피우는,자존심 있는," +
            "현실주의 적인,재능 있는,악의 있는,실질적인,완고한,못 마땅한,억압적인,끈기 있는,빈정거리는,사려 깊은,비꼬는 인정 많은,신랄한,참을성 있는," +
            "애처로운,믿을만한,성숙한,동정심이 있는,튼튼한,이해심이 있는,허약한,기략이 풍부한,자기 본위적인,공상적인,관능적인,로맨틱한,감각적인,낭만적인," +
            "성실한,자존심이 강한,빈틈없는,자만심이 강한,기민한,활발한,약삭빠른,명랑한,내성적인,재주가 많은,수줍은,변덕스러운,겁 많은,따뜻한").split(",");

    @Autowired
    ScorePublisher scorePublisher;

    @PostConstruct
    private void init() {
        this.names = new ConcurrentHashMap<>();
        this.scores = new ConcurrentHashMap<>();
    }

    public void next(String sid) {
        scores.computeIfPresent(names.get(sid), (k, v) -> {
            int score = v + 1;
            scores.replace(k, score);
            scorePublisher.next(new HashMap<>(scores));
            return score;
        });
    }

    public void join(String sid) {
        String name = genName();

        while (names.containsKey(name)) {
            name = genName();
        }

        names.put(sid, name);
        scores.put(name, 0);
        scorePublisher.next(new HashMap<>(scores));
    }

    public void leave(String sid) {
        scores.remove(names.get(sid));
        names.remove(sid);
        scorePublisher.next(new HashMap<>(scores));
    }

    public String getName(String sid) {
        return names.get(sid);
    }

    private String genName() {
        Random random = new Random();
        String prefix = PREFIXES[random.nextInt(PREFIXES.length)];
        return prefix + " 개발자";
    }
}






