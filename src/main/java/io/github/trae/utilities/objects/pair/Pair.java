package io.github.trae.utilities.objects.pair;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A generic pair of two values.
 *
 * @param <First>  the type of the first value
 * @param <Second> the type of the second value
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Pair<First, Second> {

    private First first;
    private Second second;
}