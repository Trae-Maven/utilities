package io.github.trae.utilities.objects.pair;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A generic pair of three values.
 *
 * @param <First>  the type of the first value
 * @param <Second> the type of the second value
 * @param <Third>  the type of the third value
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TriPair<First, Second, Third> {

    private First first;
    private Second second;
    private Third third;
}