package org.chrismaloney

class Song {
    String title
    String artist
    Album album

    static constraints = {
        title blank: false
        artist blank: false
    }
    
    String toString() {
        "${title}, by ${artist}"
    }
}
