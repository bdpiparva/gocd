/*
 * Copyright 2019 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// This is included from the stage details page to show the stage duration graph in a specified tree
// Invoked when when a template that is associated with a pipeline see `_stage_details_chart.html.erb`

import Chart from 'chart.js';
import 'chartjs-plugin-zoom';
import _ from 'lodash';
import {timeFormatter} from "../helpers/time_formatter";

enum DurationScale {
    Second = 1000,
    Minute = Second * 60
}

type Status = "Passed" | "Failed";

interface Data {
    pipeline_counter: number;
    status: Status;
    stage_link: string;
    duration: number;
    schedule_date: string;
    pipeline_label: string;
}

function chartDataForCounter(chartData: Data[], pipelineCounter: number): Data {
    return chartData.find((datum) => datum.pipeline_counter === pipelineCounter)!;
}

// @ts-ignore
window.showStageDurationGraph = (title: string,
                                 canvasElement: HTMLCanvasElement,
                                 chartData: Data[],
                                 zoomButton: HTMLElement) => {

    const maxDuration = _.maxBy(chartData, (datum) => datum.duration)!.duration;
    const scale = maxDuration >= DurationScale.Minute ? DurationScale.Minute : DurationScale.Second;

    const config: Chart.ChartConfiguration = {
        type: "line",
        data: {
            labels: chartData.map((datum) => datum.pipeline_counter.toString()),
            datasets: [
                {
                    label: 'Passed',
                    borderColor: '#78C42D',
                    data: chartData.map((datum) => datum.status === 'Passed' ? datum.duration / scale : undefined),
                    fill: false,
                    pointRadius: 3,
                    pointHoverRadius: 6,
                },
                {
                    label: 'Failed',
                    borderColor: '#FA2D2D',
                    data: chartData.map((datum) => datum.status === 'Failed' ? datum.duration / scale : undefined),
                    fill: false,
                    pointRadius: 3,
                    pointHoverRadius: 6,
                },
            ]
        },
        options: {
            title: {
                display: true,
                text: title,
                position: "top"
            },
            tooltips: {
                enabled: true,
                intersect: false,
                backgroundColor: 'white',
                borderColor: 'black',

                titleFontColor: 'black',
                footerFontColor: 'black',
                bodyFontColor: 'black',

                bodyFontStyle: 'bold',
                titleFontStyle: 'bold',
                footerFontStyle: 'bold',

                displayColors: false,

                borderWidth: 1,
                callbacks: {
                    title(item: Chart.ChartTooltipItem[], data: Chart.ChartData): string | string[] {
                        const selectedPipelineCounter = parseInt(item[0].xLabel! as string, 10);
                        const selectedPipelineData = chartDataForCounter(chartData, selectedPipelineCounter);
                        return `Pipeline Label: ${selectedPipelineData.pipeline_label}`;
                    },
                    label(tooltipItem: Chart.ChartTooltipItem, data: Chart.ChartData): string | string[] {
                        const pipelineCounter = parseInt(tooltipItem.xLabel! as string, 10);
                        const selectedPipelineData = chartDataForCounter(chartData, pipelineCounter);
                        const duration = timeFormatter.formattedDuration(selectedPipelineData.duration);
                        return `${selectedPipelineData.status} in ${(duration)}`;
                    },
                    footer(item: Chart.ChartTooltipItem[], data: Chart.ChartData): string | string[] {
                        const pipelineCounter = parseInt(item[0].xLabel! as string, 10);
                        const selectedPipelineData = chartDataForCounter(chartData, pipelineCounter);
                        return `Started at ${timeFormatter.format(selectedPipelineData.schedule_date)}`;
                    }
                }
            },
            scales: {
                xAxes: [
                    {
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: 'Pipeline Counter'
                        },
                        gridLines: {
                            display: false
                        }
                    }
                ],
                yAxes: [
                    {
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: `Stage Duration (${scale === DurationScale.Minute ? 'mins' : 'secs'})`
                        }
                    }
                ]
            },
            plugins: {
                zoom: {
                    zoom: {
                        enabled: true,
                        drag: true,
                        mode: 'xy',
                        onZoomComplete() {
                            zoomButton.style.display = '';
                        }
                    }
                }
            },
            onClick(event?: MouseEvent, activeElements?: Array<{}>): any {
                const elementAtEvent: any = chart.getElementAtEvent(event)[0];
                if (elementAtEvent) {
                    const pipelineCounter = parseInt(chart.data.labels![elementAtEvent._index] as string, 10);
                    const selectedPipelineData = chartDataForCounter(chartData, pipelineCounter);
                    window.location.href = selectedPipelineData.stage_link;
                }
            },
            onHover(event: MouseEvent, activeElements: Array<{}>): any {
                const elementAtEvent: any = chart.getElementAtEvent(event)[0];
                if (elementAtEvent) {
                    canvasElement.style.cursor = 'pointer';
                } else {
                    canvasElement.style.cursor = 'default';
                }
            }
        },

    };

    const chart = new Chart.Chart(canvasElement, config);
    zoomButton.style.display = 'none';
    zoomButton.addEventListener('click', (e) => {
        zoomButton.style.display = 'none';
        (chart as any).resetZoom();
        e.preventDefault()
        return false;
    });
};
