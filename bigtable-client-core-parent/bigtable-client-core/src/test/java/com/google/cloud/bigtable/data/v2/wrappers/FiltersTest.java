/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.bigtable.data.v2.wrappers;

import static com.google.cloud.bigtable.data.v2.wrappers.Filters.FILTERS;

import java.util.concurrent.TimeUnit;

import com.google.bigtable.v2.ColumnRange;
import com.google.bigtable.v2.RowFilter;
import com.google.bigtable.v2.RowFilter.Chain;
import com.google.bigtable.v2.RowFilter.Condition;
import com.google.bigtable.v2.RowFilter.Interleave;
import com.google.bigtable.v2.ValueRange;
import com.google.protobuf.ByteString;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FiltersTest {
  @Test
  public void chainTest() {
    RowFilter actualProto =
        FILTERS.chain()
            .filter(FILTERS.key().regex(".*"))
            .filter(FILTERS.key().sample(0.5))
            .filter(FILTERS.chain()
                .filter(FILTERS.family().regex("hi$"))
                .filter(FILTERS.qualifier().regex("^q")))
            .toProto();

    RowFilter expectedFilter =
        RowFilter.newBuilder()
            .setChain(
                Chain.newBuilder()
                    .addFilters(
                        RowFilter.newBuilder().setRowKeyRegexFilter(ByteString.copyFromUtf8(".*")))
                    .addFilters(RowFilter.newBuilder().setRowSampleFilter(0.5))
                    .addFilters(
                        RowFilter.newBuilder()
                            .setChain(
                                Chain.newBuilder()
                                    .addFilters(
                                        RowFilter.newBuilder().setFamilyNameRegexFilter("hi$"))
                                    .addFilters(
                                        RowFilter.newBuilder().setColumnQualifierRegexFilter(
                                            ByteString.copyFromUtf8("^q"))))))
            .build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void interleaveTest() {
    RowFilter actualProto =
        FILTERS.interleave()
            .filter(FILTERS.key().regex(".*"))
            .filter(FILTERS.key().sample(0.5))
            .filter(FILTERS.interleave()
                .filter(FILTERS.family().regex("hi$"))
                .filter(FILTERS.qualifier().regex("^q")))
            .toProto();

    RowFilter expectedFilter =
        RowFilter.newBuilder()
            .setInterleave(
                Interleave.newBuilder()
                    .addFilters(
                        RowFilter.newBuilder().setRowKeyRegexFilter(ByteString.copyFromUtf8(".*")))
                    .addFilters(RowFilter.newBuilder().setRowSampleFilter(0.5))
                    .addFilters(
                        RowFilter.newBuilder()
                            .setInterleave(
                                Interleave.newBuilder()
                                    .addFilters(
                                        RowFilter.newBuilder().setFamilyNameRegexFilter("hi$"))
                                    .addFilters(
                                        RowFilter.newBuilder().setColumnQualifierRegexFilter(
                                            ByteString.copyFromUtf8("^q"))))))
            .build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void conditionTest() {
    RowFilter actualProto =
        FILTERS.condition(FILTERS.key().regex(".*"))
            .then(FILTERS.label("true"))
            .otherwise(FILTERS.label("false"))
            .toProto();

    RowFilter expectedFilter =
        RowFilter.newBuilder()
            .setCondition(
                Condition.newBuilder()
                    .setPredicateFilter(
                        RowFilter.newBuilder().setRowKeyRegexFilter(ByteString.copyFromUtf8(".*")))
                    .setTrueFilter(RowFilter.newBuilder().setApplyLabelTransformer("true"))
                    .setFalseFilter(RowFilter.newBuilder().setApplyLabelTransformer("false")))
            .build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void passTest() {
    RowFilter actualProto = FILTERS.pass().toProto();

    RowFilter expectedFilter = RowFilter.newBuilder().setPassAllFilter(true).build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void blockTest() {
    RowFilter actualProto = FILTERS.block().toProto();

    RowFilter expectedFilter = RowFilter.newBuilder().setBlockAllFilter(true).build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void sinkTest() {
    RowFilter actualProto = FILTERS.sink().toProto();

    RowFilter expectedFilter = RowFilter.newBuilder().setSink(true).build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void labelTest() {
    RowFilter actualProto = FILTERS.label("my-label").toProto();

    RowFilter expectedFilter = RowFilter.newBuilder().setApplyLabelTransformer("my-label").build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void keyRegexTest() {
    RowFilter actualProto = FILTERS.key().regex(".*").toProto();

    RowFilter expectedFilter =
        RowFilter.newBuilder().setRowKeyRegexFilter(ByteString.copyFromUtf8(".*")).build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void keyExactMatchTest() {
    RowFilter actualProto = FILTERS.key().exactMatch(ByteString.copyFromUtf8(".*")).toProto();

    RowFilter expectedFilter =
        RowFilter.newBuilder().setRowKeyRegexFilter(ByteString.copyFromUtf8("\\.\\*")).build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void keySampleTest() {
    RowFilter actualProto = FILTERS.key().sample(0.3).toProto();

    RowFilter expectedFilter = RowFilter.newBuilder().setRowSampleFilter(0.3).build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void familyRegexTest() {
    RowFilter actualProto = FILTERS.family().regex("^hi").toProto();

    RowFilter expectedFilter = RowFilter.newBuilder().setFamilyNameRegexFilter("^hi").build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void familyExactMatchTest() {
    RowFilter actualProto = FILTERS.family().exactMatch("^hi").toProto();

    RowFilter expectedFilter = RowFilter.newBuilder().setFamilyNameRegexFilter("\\^hi").build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void qualifierRegexTest() {
    RowFilter actualProto = FILTERS.qualifier().regex("^hi").toProto();

    RowFilter expectedFilter =
        RowFilter.newBuilder()
            .setColumnQualifierRegexFilter(ByteString.copyFromUtf8("^hi"))
            .build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void qualifierExactMatchTest() {
    RowFilter actualProto = FILTERS.qualifier().exactMatch(ByteString.copyFromUtf8("^hi")).toProto();

    RowFilter expectedFilter =
        RowFilter.newBuilder()
            .setColumnQualifierRegexFilter(ByteString.copyFromUtf8("\\^hi"))
            .build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void qualifierRangeOpenClosed() {
    String family = "family";
    ByteString begin = ByteString.copyFromUtf8("begin");
    ByteString end = ByteString.copyFromUtf8("end");
    RowFilter actualProto = FILTERS.qualifier().rangeWithinFamily(family)
        .startOpen(begin)
        .endClosed(end)
        .toProto();
    RowFilter expectedFilter =
        RowFilter.newBuilder()
            .setColumnRangeFilter(
                ColumnRange.newBuilder()
                    .setFamilyName(family)
                    .setStartQualifierOpen(begin)
                    .setEndQualifierClosed(end))
            .build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void qualifierRangeClosedOpen() {
    String family = "family";
    ByteString begin = ByteString.copyFromUtf8("begin");
    ByteString end = ByteString.copyFromUtf8("end");
    RowFilter actualProto = FILTERS.qualifier().rangeWithinFamily(family)
        .startClosed(begin)
        .endOpen(end)
        .toProto();
    RowFilter expectedFilter =
        RowFilter.newBuilder()
            .setColumnRangeFilter(
                ColumnRange.newBuilder()
                    .setFamilyName(family)
                    .setStartQualifierClosed(begin)
                    .setEndQualifierOpen(end))
            .build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void valueRegex() {
    RowFilter actualProto = FILTERS.value().regex("some[0-9]regex").toProto();

    RowFilter expectedFilter =
        RowFilter.newBuilder()
            .setValueRegexFilter(ByteString.copyFromUtf8("some[0-9]regex"))
            .build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void valueExactMatch() {
    RowFilter actualProto =
        FILTERS.value().exactMatch(ByteString.copyFromUtf8("some[0-9]regex")).toProto();

    RowFilter expectedFilter =
        RowFilter.newBuilder()
            .setValueRegexFilter(ByteString.copyFromUtf8("some\\[0\\-9\\]regex"))
            .build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void valueRangeClosedOpen() {
    ByteString begin = ByteString.copyFromUtf8("begin");
    ByteString end = ByteString.copyFromUtf8("end");

    RowFilter actualProto = FILTERS.value().range()
        .startOpen(begin)
        .endClosed(end)
        .toProto();

    RowFilter expectedFilter =
        RowFilter.newBuilder()
            .setValueRangeFilter(
                ValueRange.newBuilder()
                    .setStartValueOpen(begin)
                    .setEndValueClosed(end))
            .build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void valueRangeOpenClosed() {
    ByteString begin = ByteString.copyFromUtf8("begin");
    ByteString end = ByteString.copyFromUtf8("end");

    RowFilter actualProto = FILTERS.value().range()
        .startClosed(begin)
        .endOpen(end)
        .toProto();

    RowFilter expectedFilter =
        RowFilter.newBuilder()
            .setValueRangeFilter(
                ValueRange.newBuilder()
                    .setStartValueClosed(begin)
                    .setEndValueOpen(end))
            .build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void valueStripTest() {
    RowFilter actualProto = FILTERS.value().strip().toProto();

    RowFilter expectedFilter = RowFilter.newBuilder().setStripValueTransformer(true).build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void offsetCellsPerRowTest() {
    RowFilter actualProto = FILTERS.offset().cellsPerRow(10).toProto();

    RowFilter expectedFilter = RowFilter.newBuilder().setCellsPerRowOffsetFilter(10).build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void limitCellsPerRowTest() {
    RowFilter actualProto = FILTERS.limit().cellsPerRow(10).toProto();

    RowFilter expectedFilter = RowFilter.newBuilder().setCellsPerRowLimitFilter(10).build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void limitCellsPerColumnTest() {
    RowFilter actualProto = FILTERS.limit().cellsPerColumn(10).toProto();

    RowFilter expectedFilter = RowFilter.newBuilder().setCellsPerColumnLimitFilter(10).build();

    Assert.assertEquals(expectedFilter, actualProto);
  }

  @Test
  public void timestampFullRangeTest() {
    long start = TimeUnit.MILLISECONDS.toMicros(System.currentTimeMillis() - 10000000);
    long end = TimeUnit.MILLISECONDS.toMicros(System.currentTimeMillis());
    RowFilter actualProto = FILTERS.timestamp().range(start, end).toProto();

    RowFilter.Builder expected = RowFilter.newBuilder();
    expected.getTimestampRangeFilterBuilder()
      .setStartTimestampMicros(start)
      .setEndTimestampMicros(end);

    Assert.assertEquals(expected.build(), actualProto);
  }

  @Test
  public void timestampStartTest() {
    long start = TimeUnit.MILLISECONDS.toMicros(System.currentTimeMillis() - 10000000);
    RowFilter actualProto = FILTERS.timestamp().range().startClosed(start).toProto();

    RowFilter.Builder expected = RowFilter.newBuilder();
    expected.getTimestampRangeFilterBuilder()
      .setStartTimestampMicros(start);

    Assert.assertEquals(expected.build(), actualProto);
  }

  @Test
  public void timestampEndTest() {
    long end = TimeUnit.MILLISECONDS.toMicros(System.currentTimeMillis());
    RowFilter actualProto = FILTERS.timestamp().range().endOpen(end).toProto();

    RowFilter.Builder expected = RowFilter.newBuilder();
    expected.getTimestampRangeFilterBuilder()
      .setEndTimestampMicros(end);

    Assert.assertEquals(expected.build(), actualProto);
  }
}
