import pandas as pd
import matplotlib.pyplot as plt

def main():
    try:
        df = pd.read_csv('benchmark_results.csv')
    except Exception as e:
        print("Error reading benchmark_results.csv:", e)
        return
        
    ns = df['N']
    exec_times = df['ExecutionTimeMs']
    total_slots = df['TotalTimeSlots']
    active_slots = df['ActiveTimeSlots']
    throughput = df['Throughput']
    
    fig, axs = plt.subplots(2, 2, figsize=(14, 10))
    fig.suptitle('CDMA Scalability Benchmarks ($N \in \{4, 8, 16, 32\}$)', fontsize=16)
    
    # Plot 1: Execution Time
    axs[0, 0].plot(ns, exec_times, marker='o', color='red', linewidth=2)
    axs[0, 0].set_title('Simulation Execution Time vs N')
    axs[0, 0].set_xlabel('Number of Stations (N)')
    axs[0, 0].set_ylabel('Execution Time (ms)')
    axs[0, 0].set_xticks(ns)
    axs[0, 0].grid(True, linestyle='--', alpha=0.7)
    
    # Plot 2: Throughput
    axs[0, 1].plot(ns, throughput, marker='s', color='blue', linewidth=2)
    axs[0, 1].set_title('Throughput vs N')
    axs[0, 1].set_xlabel('Number of Stations (N)')
    axs[0, 1].set_ylabel('Throughput (bits/sec)')
    axs[0, 1].set_xticks(ns)
    axs[0, 1].grid(True, linestyle='--', alpha=0.7)
    
    # Plot 3: Time Slots (Total vs Active)
    width = 2
    axs[1, 0].bar(ns - width/2, total_slots, width=width, label='Total Slots', color='skyblue', edgecolor='black')
    axs[1, 0].bar(ns + width/2, active_slots, width=width, label='Active Slots', color='orange', edgecolor='black')
    axs[1, 0].set_title('Total vs Active Time Slots')
    axs[1, 0].set_xlabel('Number of Stations (N)')
    axs[1, 0].set_ylabel('Number of Slots')
    axs[1, 0].set_xticks(ns)
    axs[1, 0].legend()
    axs[1, 0].grid(axis='y', linestyle='--', alpha=0.7)
    
    # Plot 4: Active Time Slots (%)
    ratios = (active_slots / total_slots) * 100
    axs[1, 1].plot(ns, ratios, marker='^', color='green', linewidth=2)
    axs[1, 1].set_title('Active Slot Percentage (%) vs N')
    axs[1, 1].set_xlabel('Number of Stations (N)')
    axs[1, 1].set_ylabel('Active Slots (%)')
    axs[1, 1].set_xticks(ns)
    axs[1, 1].set_ylim(0, 110)
    axs[1, 1].grid(True, linestyle='--', alpha=0.7)
    
    plt.tight_layout(rect=[0, 0.03, 1, 0.95])
    plt.savefig('cdma_benchmark_plots.png', dpi=300)
    print("Plots successfully generated and saved to cdma_benchmark_plots.png!")

if __name__ == "__main__":
    main()
