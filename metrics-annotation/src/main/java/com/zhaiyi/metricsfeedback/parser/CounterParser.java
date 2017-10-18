package com.zhaiyi.metricsfeedback.parser;

import com.codahale.metrics.Counter;
import com.zhaiyi.metricsfeedback.action.Action;
import com.zhaiyi.metricsfeedback.annotation.CounterConfig;
import com.zhaiyi.metricsfeedback.configuration.FeedbackConfiguration;
import com.zhaiyi.metricsfeedback.constants.MetricType;
import com.zhaiyi.metricsfeedback.util.MetricRegistryUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Created by zhaiyi on 2017/10/17.
 */

@Aspect
public class CounterParser extends BaseParser {

    @Around("all() && @annotation(cc)")
    public Object parse(ProceedingJoinPoint joinPoint, CounterConfig cc) throws Throwable {
        Counter counter;
        String metricName = getMetricName(joinPoint);
        if (!annotationMetrics.containsKey(metricName)) {
            //注册counter
            counter = MetricRegistryUtil.getMetricRegistry().counter(metricName);
            annotationMetrics.put(metricName, counter);
            //注册configuration
            if (cc != null && cc.action() != null) {
                FeedbackConfiguration.Builder builder = new FeedbackConfiguration.Builder();
                builder.type(MetricType.COUNTER).metricName(metricName)
                        .period(cc.period()).action((Action) cc.action().newInstance());
                if (cc.thresholds() != null) {
                    setThresholds(builder, cc.thresholds());
                }
                FeedbackConfiguration fc = builder.build();
                feedbackManager.addConfiguration(fc);
            }
        }
        counter = (Counter) annotationMetrics.get(metricName);
        Object result = joinPoint.proceed();
        counter.inc();
        return result;
    }

}
