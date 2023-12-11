import React, { useState, useRef, useEffect} from 'react';
import './App.css';
import { json } from 'stream/consumers';

function App() {

  const numLettersForWord = 5;

  const numAttempts = 6;

  const [inputLetters, setInputLetters] = useState([
    ["", "", "", "", ""],
    ["", "", "", "", ""],
    ["", "", "", "", ""],
    ["", "", "", "", ""],
    ["", "", "", "", ""],
    ["", "", "", "", ""]
  ]);

  const [inputLetterBGColors, setInputLetterBGColors] = useState([
    ["white", "white", "white", "white", "white"],
    ["gray", "gray", "gray", "gray", "gray"],
    ["gray", "gray", "gray", "gray", "gray"],
    ["gray", "gray", "gray", "gray", "gray"],
    ["gray", "gray", "gray", "gray", "gray"],
    ["gray", "gray", "gray", "gray", "gray"]
  ]);

  const [currentRow, setCurrentRow] = useState(0);
  const [currentCol, setCurrentCol] = useState(0);

  const [finalMessage, setFinalMessage] = useState('');
  const [submitButtonEnabled, setSubmitButtonEnabled] = useState(false);

  const [wordToGuess, setWordToGuess] = useState('');
  const [currentWordMap, setCurrentWordMap] = useState<Map<string, Set<number>> | undefined>(new Map());

  const isCurrentCell = (row: number, col: number) => (currentRow == row && currentCol == col);

  const isMounted = useRef(false);

  const inputRefs = useRef<Array<Array<HTMLInputElement | null>>>([]);

  useEffect(() => {
    if (!isMounted.current) { //useEffect was being called twice, and I couldn't figure out why. As a result, I added this flag to make sure it's only called once
      isMounted.current = true;
      return;
    }

    setInputRefs();
    startNewRound();
  }, []);

  const startNewRound = () => {
    const inputLetters: string[][] = [];
    const inputLetterBGColors: string[][] = [];

    for (let i = 0; i < numAttempts; i ++) {
      let inputLettersNewRow = []
      let bgColorsNewRow = []
      for (let j = 0; j < numLettersForWord; j ++) {
        inputLettersNewRow.push('');
        bgColorsNewRow.push(i == 0 ? 'white' : 'gray');
      }
      inputLetters.push(inputLettersNewRow);
      inputLetterBGColors.push(bgColorsNewRow);
    }
    setInputLetters(inputLetters);
    setInputLetterBGColors(inputLetterBGColors);


    setCurrentRow(0);
    setCurrentCol(0);
    setFinalMessage('');
    setSubmitButtonEnabled(false);
    fetchNewWord();
  }

  const fetchNewWord = async () => {
    try {
      const apiUrl = 'https://api.datamuse.com/words?sp=?????';
      const response = await fetch(apiUrl);

      if (response.ok) {
        const jsonResponse = await response.json();
        const randomWord = getRandomWordFromList(jsonResponse)['word'];
        console.log("Word for player to guess: " + randomWord);
        setCurrentWordMap(createMapOfWordLetters(randomWord));
        setWordToGuess(randomWord);
      } else {
        console.error('Failed to retrieve a word. HTTP Status:', response.status);
      }
    } catch (error) {
      console.error('Error fetching data:', error);
    }
  }

  const getRandomWordFromList = (jsonResponse: any) => {
    if (jsonResponse && jsonResponse.length > 0) {
      const randomIndex = Math.floor(Math.random() * jsonResponse.length);
      return jsonResponse[randomIndex];
    }
    return '';
  };

  const createMapOfWordLetters = (word: string) => {
    const lettersMap = new Map<string, Set<number>>();
    for (let i = 0; i < word.length; i ++) {
      let currLetter = word[i];

      let currLetterAllIndicesInWord = lettersMap.has(currLetter) ? lettersMap.get(currLetter) : new Set<number>();
      currLetterAllIndicesInWord?.add(i);
      if (currLetterAllIndicesInWord) {
        lettersMap.set(currLetter, currLetterAllIndicesInWord);
      }
    }
    console.log(lettersMap);
    return lettersMap;
  }


  const setInputRefs = () => {
    inputRefs.current = Array.from({ length: inputLetters.length }, 
      (_, rowNumber) => Array.from({ length: inputLetters[rowNumber].length }, 
      (_, colNumber) => inputRefs.current[rowNumber]?.[colNumber] || null)
    );

    setNewTextFocus(0, 0);
  };

  const handleInputChange = (row: number, col: number, event: React.ChangeEvent<HTMLInputElement>) => {
    const inputValue = event.target.value.charAt(0); // Get the first character

    const newArray = [...inputLetters];
    newArray[row][col] = inputValue;
    setInputLetters(newArray);

    // Update currentCol when a character is entered
    if (inputValue) {
      let newCol = currentCol

      if (newCol < numLettersForWord) {
        if (newCol == numLettersForWord - 1) {
          setSubmitButtonEnabled(true);
        } else {
          newCol = newCol + 1;
          setCurrentCol(newCol);

          setNewTextFocus(row, newCol);
        }
      }
    }
  };

  const setNewTextFocus = (row: number, col: number) => {
    inputRefs.current[row][col]?.focus();
  }

  const submitWord = () => {
    let word = ""
    for (let i = 0; i < numLettersForWord; i ++) {
      word += inputLetters[currentRow][i];
    }

    console.log("User inputted: " + word);

    const numCorrectLetters = evalUserSubmittedLetters();
    if(numCorrectLetters == numLettersForWord) {
      setFinalMessage("Guessed the word correctly!");
    } else if (currentRow == numAttempts - 1) {
      setFinalMessage("Did not get the word correct. The word was: " + wordToGuess);
    } else {
      goToNextRow();
    }
    setSubmitButtonEnabled(false);
  }

  const evalUserSubmittedLetters = () => {
    let numCorrectLetters = 0
    const newArray = [...inputLetterBGColors];
    for (let i = 0; i < numLettersForWord; i ++) {
      let currLetter = inputLetters[currentRow][i];
      let newBGColor = 'white';
      if (currentWordMap?.has(currLetter)) {
        if (currentWordMap?.get(currLetter)?.has(i)) {
          newBGColor = 'green';
          numCorrectLetters ++;
        } else {
          newBGColor = 'orange';
        }
      } else {
        newBGColor = 'gray';
      }

      newArray[currentRow][i] = newBGColor;
    }

    setInputLetterBGColors(newArray);

    return numCorrectLetters;
  }

  const goToNextRow = () => {
    let nextRow = currentRow + 1;

    const newArray = [...inputLetterBGColors];
    for (let i = 0; i < newArray.length; i ++) {
      newArray[nextRow][i] = 'white';
    }
    setInputLetterBGColors(newArray);

    setCurrentRow(nextRow);
    setCurrentCol(0);
    
    setNewTextFocus(nextRow, 0);
  }

  const clearCell = () => {
    const newArray = [...inputLetters];
    newArray[currentRow][currentCol] = "";
    setInputLetters(newArray);
  }

  const goToPreviousCell = () => {
    if (currentCol > 0) {
      let newCol = currentCol - 1;
      setCurrentCol(newCol);
      setNewTextFocus(currentRow, newCol);
    }
  }

  const goToNextCell = () => {
    if (currentCol < numLettersForWord - 1) {
      let newCol = currentCol + 1;
      setCurrentCol((prevCol) => prevCol + 1);
      setNewTextFocus(currentRow, newCol);
    }
  }

  return (
    <div>
      <div>
      {inputLetters.map((row, rowNumber) => (
        <div key={rowNumber}>
          {row.map((element, colNumber) => (
            <input 
              type="text"
              style={{
                borderRadius:20,
                width:100,
                height:100,
                backgroundColor: inputLetterBGColors[rowNumber][colNumber],
                borderColor: isCurrentCell(rowNumber, colNumber) ? "red": "black",
                borderWidth:5
              }} 
              readOnly={!isCurrentCell(rowNumber, colNumber)} // Make input editable
              value={element}
              maxLength={1} // Limit input to one character
              onChange={(event) => handleInputChange(rowNumber, colNumber, event)}
              ref={(ref) => {
                inputRefs.current[rowNumber] = inputRefs.current[rowNumber] || [];
                inputRefs.current[rowNumber][colNumber] = ref;
              }}
            />
          ))}
        </div>
      ))}
      </div>
      <p></p>
      <p></p>
      <div>
        <button disabled={!submitButtonEnabled} onClick={submitWord}>Submit</button>
      </div>
      <div>
      <button onClick={goToPreviousCell}>Previous cell</button>
      <button onClick={goToNextCell}>Next cell</button>
      <button onClick={clearCell}>Clear current cell</button>
      <p></p>
      <p>{finalMessage}</p>
      <div>
        <button disabled={(finalMessage == '')} onClick={startNewRound}>Start New Round</button>
      </div>
      </div>
    </div>
  );
}

export default App;
