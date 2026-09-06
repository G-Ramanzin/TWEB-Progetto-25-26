/**
 * @fileoverview Client-side behaviour of Anime Explorer.
 *
 * Charts are declared in the HTML through data-* attributes on
 * <canvas> elements (data-chart, data-labels, data-values, data-label)
 * and initialised here with Chart.js. This keeps the views free of
 * inline scripts and the data flow easy to follow.
 */

/**
 * Builds one Chart.js chart from a canvas element's data attributes.
 *
 * @param {HTMLCanvasElement} canvas - canvas carrying data-* config
 * @returns {void}
 */
function initChart(canvas) {
    var kind = canvas.dataset.chart;
    var labels = JSON.parse(canvas.dataset.labels || '[]');
    var values = JSON.parse(canvas.dataset.values || '[]');
    var label = canvas.dataset.label || '';

    var palette = [
        '#4a2a6b', '#7b3fa0', '#ffb400', '#e63946', '#2a9d8f',
        '#264653', '#f4a261', '#457b9d', '#8d99ae', '#6a994e',
        '#bc4749', '#3a86ff', '#8338ec', '#fb5607', '#ff006e'
    ];

    var config;
    if (kind === 'doughnut') {
        config = {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{ data: values, backgroundColor: palette }]
            },
            options: { responsive: true, maintainAspectRatio: false }
        };
    } else {
        var horizontal = kind === 'horizontalBar';
        config = {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: label,
                    data: values,
                    backgroundColor: horizontal ? palette : '#7b3fa0'
                }]
            },
            options: {
                indexAxis: horizontal ? 'y' : 'x',
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: !horizontal } }
            }
        };
    }

    new Chart(canvas, config);
}

document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('canvas[data-chart]').forEach(initChart);
});
