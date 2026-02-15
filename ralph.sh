set -e

if [ -z "$1" ]; then
  echo "Usage: $0 <iterations>"
  exit 1
fi

for ((i=1; i<=$1; i++)); do
    echo "Iteration $i"
    echo "-------------------------"
    result=$(copilot --allow-all --resume --model "claude-opus-4.6" -p "@plans/prd.json @progress.txt \
1. Find the highest priority feature to work on and only work on that feature. \
This should be the one YOU decide has the highest priority - not necessarily what is first in the list. \
2. Implement the feature within the JavaService codebase using Dropwizard as our Framework. \
3. Update the PRD with the work that was done. \
4. Append your progress to progress.txt. \
5. Make a git commit of that feature. \
ONLY WORK ON A SINGLE FEATURE PER ITERATION. \
If, while implementing a feature, you notice the PRD is complete, output <promise>COMPLETE</promise>. \
  " )

    echo "$result"

    if [[ "$result" == *"<promise>COMPLETE</promise>"* ]]; then
        echo "All features have been implemented. Exiting."
        exit 0
    fi
done
