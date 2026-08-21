package com.shopmanagement.jyotishservice.service.report;

import java.util.Map;

/** One NI chart cell for the multi-varga PDF grid. */
public record VargaChartFrame(String title, int lagnaSignIndex, Map<String, Integer> houseOfPlanet) {}
