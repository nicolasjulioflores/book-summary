# **README for Book Summary App**

Creators: Nicolas Julio Flores and Ananta Prabin Karkin (Team 25)
email: nicolas.j.flores.19@dartmouth.edu
email: ananta.karki.ug@dartmouth.edu

### Goal
Often times nowadays, students are overcommitted and lack the time to thoroughly read all the documents they are assigned. To aid with this, we developed an app that will allow students to take pictures of their readings and return a summary to it, after submitting it to an online summary API.

# **Functionality**

### Main Menu
When first opening the app, the user is greeted by a home tutorial screen that shows whenever the app is opened. This screen indicates to the user their two primary actions: switching between libraries and adding a new text. Switching between libraries is done via a ```Navigation View```, which opens a sidebar when clicked. The user has 3 options on this navigation page: *Texts Library*, *Summaries Library*, and *About*. The texts library has all the saved texts. The summaries tab contains all the summaries, and the about page has our short proposal and motivation for the project. The second action the user can take is through the floating action button with the **+**. This starts the `NewTextActivity` for the user. The user can also enter this activity by clicking any of their saved texts or summaries, but instead of the default of no text, their saved text or summary will be their. Finally, on the menu for either texts or summaries, users can tap the trash can to delete any of their saved texts.This deletes the saved text file associated with that text or summary and clears the title (key) and text path (value) from the SharedPreferences file for the app. This delete action supports multiple deletes.

### [API 1: Text Recognition by Google](https://developers.google.com/vision/text-overview)
This API parses text from a `Frame` type and returns a `SparseArray<TextBlock>`, which essentially captures text from an image. Because there are blocks of an image where there will not be text (say because of an indentation), the array is hence sparse. From this image, we simply pick out the spots that do have words and copy them into a single `StringBuilder`, which houses the text. 

The users experience this in the app when they are in the `NewTextActivity` and hit the camera `FloatingActionButton`. This opens the camera, and allows the user to take a picture of their text. Currently the landscape orientation of picture taking is not possible as it is not properly read by the text recognition API. After taking a picture, they are sent to a `CropActivity`, where they can remove some of the artifacts from their image to make the text recognizer pick up only the important parts of the image. Finally, their text is processed in an `AsyncTask` to avoid screen lag. Once their text is returned, it is editable.

From here, the user can either save or summarize their text. If they save their text, they are simply returned to the `NavigationView`. 

### [API 2: Summarize Text by Intellexer](https://www.intellexer.com/summarizer.html)
If the user clicks the send button, they are prompted for how many sentences they would like their summary to be. If the request is properly answered by the server, a summary will be returned in roughly 4 seconds for the one to two pages of *real* text that we gave it. The user can then save their summary, which will add a new `CardView` to the summaries library under the title of the `text` that was summarized. The user will be told they cannot summarize if they are missing either a title or their text is blank.

The `NewTextActivity` posts an `HTTP request` to the server and then receives the response in a `JSONArray`. The response is read and then displayed to the user. The Intellexer Summarize Text API that we used is free for up to 500 summaries a month.


# Design
We heavily emphasized material design in our app layout. We chose card views to be the saved texts or summaries in the library because we wanted to create a flat, rectangular feel to the app. In that vain, we strived to only use `Snackbars` instead of `Toasts`. To obtain our color palette, we chose a primary color we liked (`#329cf2`) and then inputted it into [ColorHexa](http://www.colorhexa.com/), which gave us the complement color for our primary that we used for the `FloatingActionButtons`. We used other suggested colors to design the color scheme. To give our app a consistent feel, we used the [Material Design Icons](https://material.io/icons/), suggested by Google.

### Resources

Text Recognition API:
https://developers.google.com/vision/text-overview

Text Summary API:
https://www.intellexer.com/summarizer.html

ColorHexa:
http://www.colorhexa.com/

Material Design Icons:
https://material.io/icons/

Circular ImageView:
https://github.com/hdodenhof/CircleImageView
