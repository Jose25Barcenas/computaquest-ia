import { useMemo, useRef } from 'react'
import { Radar } from 'react-chartjs-2'
import { Chart as ChartJS, RadialLinearScale, PointElement, LineElement, Filler, Tooltip, Legend } from 'chart.js'

ChartJS.register(RadialLinearScale, PointElement, LineElement, Filler, Tooltip, Legend)

export default function RadarChart({ progress = [] }) {
  const chartRef = useRef(null)

  const scores = useMemo(() => {
    const skills = {
      decomposition: { total: 0, completed: 0 },
      patterns: { total: 0, completed: 0 },
      abstraction: { total: 0, completed: 0 },
      algorithms: { total: 0, completed: 0 },
    }

    progress.forEach(p => {
      if (p.challengeType) {
        const type = p.challengeType.toLowerCase()
        if (skills[type]) {
          skills[type].total++
          if (p.completed) skills[type].completed++
        }
      }
    })

    return [
      skills.decomposition.total > 0 ? (skills.decomposition.completed / skills.decomposition.total) * 100 : 0,
      skills.patterns.total > 0 ? (skills.patterns.completed / skills.patterns.total) * 100 : 0,
      skills.abstraction.total > 0 ? (skills.abstraction.completed / skills.abstraction.total) * 100 : 0,
      skills.algorithms.total > 0 ? (skills.algorithms.completed / skills.algorithms.total) * 100 : 0,
    ]
  }, [progress])

  const isDark = document.documentElement.getAttribute('data-theme') !== 'light'
  const labelColor = isDark ? 'rgba(255,255,255,0.8)' : 'rgba(0,0,0,0.7)'
  const gridColor = isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)'
  const tickColor = isDark ? 'rgba(255,255,255,0.5)' : 'rgba(0,0,0,0.5)'

  const data = useMemo(() => ({
    labels: ['Descomposicion', 'Patrones', 'Abstraccion', 'Algoritmos'],
    datasets: [{
      label: 'Habilidades',
      data: scores,
      backgroundColor: 'rgba(99, 102, 241, 0.2)',
      borderColor: 'rgba(99, 102, 241, 0.8)',
      borderWidth: 2,
      pointBackgroundColor: 'rgba(99, 102, 241, 1)',
      pointBorderColor: isDark ? '#fff' : '#333',
      pointHoverBackgroundColor: isDark ? '#fff' : '#333',
      pointHoverBorderColor: 'rgba(99, 102, 241, 1)',
    }]
  }), [scores, isDark])

  const options = useMemo(() => ({
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      r: {
        beginAtZero: true,
        max: 100,
        ticks: {
          stepSize: 20,
          color: tickColor,
          backdropColor: 'transparent',
        },
        grid: { color: gridColor },
        angleLines: { color: gridColor },
        pointLabels: {
          color: labelColor,
          font: { size: 11 },
        },
      },
    },
    plugins: {
      legend: { display: false },
    },
  }), [labelColor, gridColor, tickColor])

  return (
    <div className="radar-chart-container">
      <h3><i className="fa-solid fa-chart-radar"></i> Habilidades</h3>
      <div className="radar-chart">
        <Radar ref={chartRef} data={data} options={options} />
      </div>
    </div>
  )
}
