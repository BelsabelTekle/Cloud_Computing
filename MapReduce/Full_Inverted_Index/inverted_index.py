from collections import defaultdict

def map_function(file_index, line):
    words = line.strip().split()
    return [(word, (file_index, 1)) for word in words]

def reduce_function(values):
    # Group and sort values by file index and position
    return sorted(values, key=lambda x: (x[0], x[1]))

def main():
    # Input files mapped to indices
    input_files = {
        'file0.txt': 0,
        'file1.txt': 1,
        'file2.txt': 2
    }

    # Map Phase
    intermediate_results = defaultdict(list)
    for file_name, index in input_files.items():
        with open(file_name, 'r') as file:
            for line in file:
                map_results = map_function(index, line)
                for word, value in map_results:
                    intermediate_results[word].append(value)

    # Reduce Phase
    inverted_index = {word: reduce_function(values) for word, values in intermediate_results.items()}

    # Print Inverted Index
    for word, index in sorted(inverted_index.items()):
        print(f"{word}: {index}")

if __name__ == "__main__":
    main()
